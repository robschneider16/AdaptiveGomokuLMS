#lang racket

(provide (all-defined-out))

;;---------------------------------------
;;  GOMOKU CLIENT
;;---------------------------------------

(define GOMOKUPORT 17033)

;;-------------------------------------------------------------------------------------------
;; protocol

;; client connects to server

;; server sends three groups of data:
;; 1. game-status as one of 'continuing, 'win, 'lose, 'draw, 'forfeit
;; 2. series of lines of characters representing each row of the current SQUARE-board; characters one of: "x", "o", or " " (space)
;; 3. color to play: either "x" or "o"

;; client sends a move:
;; 1. single line consisting of space separated row and column values
;;-------------------------------------------------------------------------------------------

;; random-move:  -> string
;; generate a random move on a 13x13 board
(define (random-move) (format "~a ~a" (random 13) (random 13)))

;; read-board: input-port -> (listof string)
;; read a SQUARE-board from the port, packaging it as list of rows (top to bottom) where each row is a string
(define (read-board iprt)
  (let ([first-row (read-line iprt)])
    (cons first-row
          (for/list ([r (sub1 (string-length first-row))]) ;; assume board is square
            (read-line iprt)))))

;; net-play-moves: input-port output-port -> symbol
;; plays a networked GOMOKU game using the given socket ports to read game-states and write move-pairs
;; finally returns one of 'win, 'lose, or 'draw
(define (net-play-moves iprt oprt)
  (let* (;read game-status/game-state/to-play-player triple
         [gstatus (string->symbol (read-line iprt))]
         [board-state (read-board iprt)]
         [to-play (read-line iprt)])
    ;; display the game-state information
    ;(displayln gstatus)
    ;(for ([row board-state]) (displayln row))
    ;(displayln to-play)
    (sleep 1.99)
    ;; send the client's move to the server
    (displayln (random-move) oprt) (flush-output oprt) ;send move to server
    ;; repeat until game over
    (if (symbol=? gstatus 'continuing)
        (net-play-moves iprt oprt)
        gstatus)))

;; net-play-game: string -> symbol
;; connect to a GomokuServer running on the given host and play a game
(define (net-play-game server-host)
  (let*-values ([(inprt oprt) (tcp-connect server-host GOMOKUPORT)]
                [(ignore1) (printf "net-play-game: connected to ~a~n" server-host)]
                [(result) (net-play-moves inprt oprt)])
    (close-output-port oprt)
    (close-input-port inprt)
    result))

(net-play-game "localhost")