#lang racket

(require lang/posn htdp/draw)

(provide (all-defined-out))

(define GOMOKUPORT 17033)

;;---------------------------------------------------------------------------
;;    GOMOKU GAME SERVER
;;---------------------------------------------------------------------------

(define IN-ROW-TO-WIN 5)
(define BOARD-SIZE 11)
(define CELL-SIZE 40)
(define STONE-RADIUS 18)
(define MARGIN (* CELL-SIZE 3/4))
(define START-GAME (build-vector BOARD-SIZE (lambda (r) (build-vector BOARD-SIZE (lambda (c) 'b)))))
(define WID/HEIGHT (+ (* 2 MARGIN) (* (sub1 BOARD-SIZE) CELL-SIZE)))
(define HIDE-GAME true)
(set! HIDE-GAME HIDE-GAME)
(define MY-COLOR 'x)(set! MY-COLOR MY-COLOR)
(define other-player-mover 'gak)(set! other-player-mover (lambda (gs) (cons 0 0)))
(define indices (build-list BOARD-SIZE (lambda (n) n)))

(start WID/HEIGHT WID/HEIGHT)

;; a Game-State (GS) is a (vectorof (vectorof symbol))

;; a move-pair is a (cons r c) where r and c are indices of the move

;; a move is a (cons GS mp), where GS is a Game-State and mp is a move-pair

;;---------------------- UTILITIES

;; reset-start-game : ->
(define (reset-start-game)
  (set! START-GAME (build-vector BOARD-SIZE (lambda (r) (build-vector BOARD-SIZE (lambda (c) 'b))))))

;; vgame-spot: GS N N -> symbol
;; the vector version
(define (vgame-spot gs r c) (vector-ref (vector-ref gs r) c))

;; place-move: GS (N . N) symbol -> GS
;; copy the given game-state and then place the given to-play player's move.
;; ASSUME the move is valid!
(define (place-move gs move-pair to-play)
  (let ([new-gs (build-vector BOARD-SIZE (lambda (r) (vector-copy (vector-ref gs r))))])
    (vector-set! (vector-ref new-gs (car move-pair)) (cdr move-pair) to-play)
    new-gs))

;; place-move! : GS (N . N) symbol -> (void)
(define (place-move! gs move-pair to-play)
  (vector-set! (vector-ref gs (car move-pair)) (cdr move-pair) to-play))

;; toggle: symbol -> symbol
;; flips from one player to the other
(define (toggle tp)
  (case tp
    [(x) 'o]
    [(o) 'x]
    [else (error 'toggle (format "invalid player to-play: ~a" tp))]))


;;--------------------- DRAWING UTILITIES

;; draw-grid: GS -> true
;; ASSUME square board
(define (draw-grid ignore-state)
  (local 
    ((define (dg-aux n)
       (cond [(zero? n) true]
             [else (and (draw-solid-line (make-posn (+ (* (sub1 n) CELL-SIZE) MARGIN) MARGIN) 
                                         (make-posn (+ (* (sub1 n) CELL-SIZE) MARGIN) 
                                                    (- WID/HEIGHT MARGIN))
                                         'black)
                        (draw-solid-line (make-posn MARGIN (+ (* (sub1 n) CELL-SIZE) MARGIN))
                                         (make-posn (- WID/HEIGHT MARGIN) 
                                                    (+ (* (sub1 n) CELL-SIZE) MARGIN))
                                         'black)
                        (dg-aux (sub1 n)))])))
    (dg-aux BOARD-SIZE)))

;; draw-x: number number -> true
;; draws a black stone on the zero-based LOGICAL INTERSECTION of the board
(define (draw-x r c)
  (local (;; draw-x-help: number number -> true
	  ;; draws a black circle at the physical SCREEN-BASED pixel coordinates specified
	  (define (draw-x-help y x)
	    (draw-solid-disk (make-posn x y) STONE-RADIUS 'black)))
    (draw-x-help (+ (* r CELL-SIZE) MARGIN) (+ (* c CELL-SIZE) MARGIN))))

;; draw-o: number number -> true
;; draws an white stone in the zero-based LOGICAL INTERSECTION of the board
(define (draw-o r c)
  (local (;; draw-o-help: number number -> true
	  ;; draws a circle at the physical SCREEN-BASED pixel coordinates specified
	  (define (draw-o-help y x)
            (and (draw-solid-disk (make-posn x y) STONE-RADIUS 'white)
                 (draw-circle (make-posn x y) STONE-RADIUS 'black))))
    (draw-o-help (+ (* r CELL-SIZE) MARGIN) (+ (* c CELL-SIZE) MARGIN))))

;; draw-game: GS -> true
;; draw the gamestate
(define (draw-game gs)
  (clear-solid-rect (make-posn 0 0) WID/HEIGHT WID/HEIGHT)
  (draw-grid gs)
  (do ([row 0 (add1 row)])
    ((= row BOARD-SIZE) true)
    (do ([col 0 (add1 col)])
      ((= col BOARD-SIZE))
      (cond [(symbol=? (vgame-spot gs row col) 'x)
             (draw-x row col)]
            [(symbol=? (vgame-spot gs row col) 'o)
             (draw-o row col)]))))


;;------------------------ GENERATE THE NEXT STATES BASED ON THE CURRENT

;; next-states: GS symbol -> (listof move)
;; generate all of the possible next-states for the player to-play next.
(define (next-states gs to-play)
  (local (;; to-row: N -> N
          ;; find the row containing the nth (0-based) item in a square grid BOARD-SIZE on edge
          (define (to-row n) (quotient n BOARD-SIZE))
          ;; to-col: N -> N
          ;; find the column containing the nth ...
          (define (to-col n) (modulo n BOARD-SIZE))
          ;; build-gs: N N -> GS
          ;; create a new game-state from gs but with the blank at r and c replaced with to-play
          (define (build-gs r0 c0)
            (build-vector BOARD-SIZE
                          (lambda (r)
                            (build-vector BOARD-SIZE
                                          (lambda (c) (if (and (= r0 r) (= c0 c))
                                                          to-play
                                                          (vgame-spot gs r c)))))))
          ;; ns-aux: N -> (listof GS)
          ;; go through all the spots and if blank, create a new state
          (define (ns-aux n)
            (local ((define r (to-row n))
                    (define c (to-col n)))
              (cond [(= n (sqr BOARD-SIZE)) empty]
                    [(symbol=? 'b (vgame-spot gs r c))
                     (cons (cons (build-gs r c) (cons r c)) (ns-aux (add1 n)))]
                    [else (ns-aux (add1 n))])))
          )
    (ns-aux 0)))


;;---------------------- EVALUATE GAME STATUS

;; game-end?: GS -> boolean
;; determine if the game is over
(define (game-end? gs)
  (not (symbol=? (game-result gs) '?)))

;; game-result: GS -> symbol['x,'o,'d,'?]
;; determine if the game is a win for either 'x or 'o, a draw 'd, or not finished '?
(define (game-result gs)
  (cond [(n-in-row? gs 'x) 'x]
        [(n-in-row? gs 'o) 'o]
        [(empty? (next-states gs 'z)) 'd]
        [else '?]))

(define (win/lose/draw gs to-play)
  (let ([res (game-result gs)])
    (case res
      [(x) (if (symbol=? 'x to-play) 'win 'lose)]
      [(o) (if (symbol=? 'o to-play) 'win 'lose)]
      [(d) 'draw]
      [else (error 'win/lose/draw "game not finished")])))

;;-----------------------------------------------------------
;; HELPER CODE FOR CHECKING N-IN-A-ROW

(define-struct espot (l ul u ur))
;; an espot is a structure: (make-espot n1 n2 n3 n4)
;; where n1 through n4 are numbers
;; the numbers represent the length of the line of stones in the respective directions

;; make-inline-grid: GS sybol -> (vectorof (vectorof espot))
;; create a grid of espots that describe to-plays (tp) in a line
(define (make-inline-grid gs tp)
  (local ((define esgrid (build-vector BOARD-SIZE (lambda (_) (build-vector BOARD-SIZE (lambda (_) 'dummy)))))
          (define (vbuild-espot r c)
            (make-espot (if (zero? c) 1 (add1 (espot-l (vector-ref (vector-ref esgrid r) (sub1 c)))))
                        (if (or (zero? c) (zero? r)) 1 (add1 (espot-ul (vector-ref (vector-ref esgrid (sub1 r)) (sub1 c)))))
                        (if (zero? r) 1 (add1 (espot-u (vector-ref (vector-ref esgrid (sub1 r)) c))))
                        (if (or (= c (sub1 BOARD-SIZE)) (zero? r)) 1 (add1 (espot-ur (vector-ref (vector-ref esgrid (sub1 r)) (add1 c)))))))
          (define (vprow! r)
            (local ((define the-row (vector-ref esgrid r)))
              (for-each (lambda (i)
                          (vector-set! the-row i 
                                       (if (symbol=? (vgame-spot gs r i) tp)
                                           (vbuild-espot r i)
                                           (make-espot 0 0 0 0))))
                        indices)))
          )
    (begin (for-each (lambda (r) (vprow! r))
                     indices)
           esgrid)))

;; n-in-row?: GS symbol -> boolean
;; determine if there are IN-ROW-TO-WIN stones of the player p in a line of the given game-state
(define (n-in-row? gs p)
  (local ((define ig (make-inline-grid gs p)))
    (positive? (vector-count 
                (lambda (row)
                  (positive? (vector-count
                              (lambda (e) (or (= IN-ROW-TO-WIN (espot-l e))
                                              (= IN-ROW-TO-WIN (espot-ul e))
                                              (= IN-ROW-TO-WIN (espot-u e))
                                              (= IN-ROW-TO-WIN (espot-ur e))))
                              row)))
                ig))))


;;------------------------------------------------------------------------------------------------------
;;----------------- Server Portion ---------------------------------------------------------------------

(define-struct player (iprt oprt))
;; a player is a structure: (make-player i o) where i is an input-port and o is an output-port

(define (get-a-listener) (tcp-listen GOMOKUPORT))

;; read-move: intput-port -> (cons N N)
;; read a player's move from the player's input-port, returning as a dotted pair
(define (read-move iprt)
  (cons (read iprt) (read iprt)))

;; send-game-info: string GS symbol output-port -> void
;; send the given game information to the given output-port
(define (send-game-info status gs to-play oprt)
  (fprintf oprt "~a~%" status)
  (for ([row gs])
    (for ([col row]) (fprintf oprt "~a" (if (symbol=? col 'b) #\space col)))
    (fprintf oprt "~%"))
  (fprintf oprt "~a~%" to-play))

;; srv-game: GS player player symbol -> 
(define (srv-game gs p1 p2 to-play)
  (draw-game gs)
  ;; unless game-over, do:
  (cond [(not (game-end? gs))
         ;; send game-status, board-state, and player-to-play to p1
         (send-game-info 'continuing gs to-play (player-oprt p1)) (flush-output (player-oprt p1))
         (let* ([start-time (current-milliseconds)]
                [a-move (read-move (player-iprt p1))]) ; read move from p1
           ;; check if 
           (cond [(> (- (current-milliseconds) start-time) 2000000)
                  (send-game-info 'forfeit-time gs to-play (player-oprt p1))
                  (send-game-info 'win gs (toggle to-play) (player-oprt p2))]
                 [(not (and (< -1 (car a-move) BOARD-SIZE) (< -1 (cdr a-move) BOARD-SIZE)
                            (symbol=? 'b (vgame-spot gs (car a-move) (cdr a-move)))))
                  (send-game-info 'forfeit-move gs to-play (player-oprt p1))
                  (send-game-info 'win gs (toggle to-play) (player-oprt p2))]
                 [else ;; move is valid (i.e., on the board and vacant)
                  (place-move! gs a-move to-play) ; and update game-state accordingly
                  ;; call srv-game with new game-state and p2 p1 swapped
                  (srv-game gs p2 p1 (toggle to-play))]))]
        [else ;; terminate with actual outcome
         (send-game-info (win/lose/draw gs to-play) gs to-play (player-oprt p1)) (flush-output (player-oprt p1))
         (send-game-info (win/lose/draw gs (toggle to-play)) gs to-play (player-oprt p2)) (flush-output (player-oprt p2))
         ]))
  
;; serve-a-game: tcp-listener -> ...
(define (serve-a-game my-listener)
  (let*-values ([(p1-iprt p1-oprt) (tcp-accept my-listener)]
                [(p2-iprt p2-oprt) (tcp-accept my-listener)])
    (printf "accepted two connections!~n")
    (reset-start-game)
    (srv-game START-GAME (make-player p1-iprt p1-oprt) (make-player p2-iprt p2-oprt) 'x)
    (close-output-port p1-oprt)
    (close-input-port p1-iprt)
    (close-output-port p2-oprt)
    (close-input-port p2-iprt)
    ;(tcp-close my-listener)
    (serve-a-game my-listener)
    ))

(serve-a-game (get-a-listener))