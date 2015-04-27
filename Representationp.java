

public class Representationp {
    
    public int Posn_X, Posn_Y;
    public Character Me;
    public Character Opponent;
    public Character[][] Grid;
    public int boardSize;
    public int[] SRep = new int[27];
    //public Random r = new Random();
    //26 variables/cases to the game that I could think of that are important to deciding a move.
    //13 for each player.
    
    public Representationp(int x, int y, Character[][] g, String player, String opp){
        Grid = g;
        Posn_X = x;
        Posn_Y = y;
        Opponent = opp.charAt(0);
        Me = player.charAt(0);
        boardSize = g.length;
    }
    
    
    public void ComputeRepresentation(){
        


        //initialize SRep;
        SRep[0]= 1; //no vaiable, w0
        for(int i = 1; i < SRep.length; i++){
            SRep[i] = 0;
        }
 
        //meValues
        int[] meV = new int[8];
        for(int i = 0; i < meV.length; i++){
            meV[i] = 0;
        }
        meV = SearchAllDirections(Me);
        // w1, # of pieces in range = 4, 4 Right, 0Left _ X X X X
        AddToRep(4,0,meV,1);
        // w2, # of pieces in range = 4, 3 Right, 1Left X _ X X X
        AddToRep(3,1,meV,2);
        // w3, # of pieces in range = 4, 2 Right, 2Left X X _ X X
        AddToRep(2,2,meV,3);
        // w4, # of pieces in range = 3, 3 Right, 0Left _ X X X 
        AddToRep(3,0,meV,4);
        // w5, # of pieces in range = 3, 2 Right, 1Left X X _ X 
        AddToRep(2,1,meV,5);
        // w6, # of pieces in range = 2, 2 Right, 0Left X X _  
        AddToRep(2,0,meV,6);
        // w7, # of pieces in range = 2, 1 Right, 1Left X _ X 
        AddToRep(1,1,meV,7);
        // w8, # of pieces in range = 1, 1 Right, 0Left _ X 
        AddToRep(1,0,meV,8);

        //opponent values
        int[] opponentValues = new int[8];
        for(int i = 0; i < opponentValues.length; i++){
            opponentValues[i] = 0;
        }
        opponentValues = SearchAllDirections(Opponent);
         // w9, # of pieces in range = 4, 4 Right, 0Left _ X X X X
        AddToRep(4,0,opponentValues,9);
        // w10, # of pieces in range = 4, 3 Right, 1Left X _ X X X
        AddToRep(3,1,opponentValues,10);
        // w11, # of pieces in range = 4, 2 Right, 2Left X X _ X X
        AddToRep(2,2,opponentValues,11);
        // w12, # of pieces in range = 3, 3 Right, 0Left _ X X X 
        AddToRep(3,0,opponentValues,12);
        // w13, # of pieces in range = 3, 2 Right, 1Left X X _ X 
        AddToRep(2,1,opponentValues,13);
        // w14, # of pieces in range = 2, 2 Right, 0Left X X _  
        AddToRep(2,0,opponentValues,14);
        // w15, # of pieces in range = 2, 1 Right, 1Left X _ X 
        AddToRep(1,1,opponentValues,15);
        // w16, # of pieces in range = 1, 1 Right, 0Left _ X 
        AddToRep(1,0,opponentValues,16);

        //Available spaces values
        int[] openValues = new int[8];
        for(int i = 0; i < openValues.length; i++){
            openValues[i] = 0;
        }
        openValues = SearchAllDirections(' ');

        // w17, # of pieces in range = 4, 4 Right, 0Left _ X X X X
        AddToRep(4,0,openValues,17);
        // w18, # of pieces in range = 4, 3 Right, 1Left X _ X X X
        AddToRep(3,1,openValues,18);
        // w19, # of pieces in range = 4, 2 Right, 2Left X X _ X X
        AddToRep(2,2,openValues,19);
        // w20, # of pieces in range = 3, 3 Right, 0Left _ X X X 
        AddToRep(3,0,openValues,20);
        // w21, # of pieces in range = 3, 2 Right, 1Left X X _ X 
        AddToRep(2,1,openValues,21);
        // w22, # of pieces in range = 2, 2 Right, 0Left X X _  
        AddToRep(2,0,openValues,22);
        // w23, # of pieces in range = 2, 1 Right, 1Left X _ X 
        AddToRep(1,1,openValues,23);
        // w24, # of pieces in range = 1, 1 Right, 0Left _ X
        AddToRep(1,0,openValues,24);

    }

    public void AddToRep(int right, int left, int gridsearch[], int i){//int i = SREP INDEX,
        if((gridsearch[0] >= right) && (gridsearch[4] >= left)){
            SRep[i] += 1;
        }
        if((gridsearch[1] == right) && (gridsearch[5] == left)){
            SRep[i] += 1;
        }
        if((gridsearch[2] == right) && (gridsearch[6] == left)){
            SRep[i] += 1;
        }
        if((gridsearch[3] == right) && (gridsearch[7] == left)){
            SRep[i] += 1;
        }
        if((gridsearch[4] == right) && (gridsearch[0] == left)){
            SRep[i] += 1;
        }
        if((gridsearch[5] == right) && (gridsearch[1] == left)){
            SRep[i] += 1;
        }
        if((gridsearch[6] == right) && (gridsearch[2] == left)){
            SRep[i] += 1;
        }
        if((gridsearch[7] == right) && (gridsearch[3] == left)){
            SRep[i] += 1;
        }
    }
    
    public int SearchOneDirection(int x, int y, int dx, int dy , Character lookingFor, int distance){
        int newx = x+dx;
        int newy = y+dy;
        int value = 0;
        if((distance > 0) && (newx >= 0) && (newy >= 0) && (newx < boardSize) && (newy < boardSize)){
           if(Grid[newx][newy] == lookingFor){
                value = value + 1;
            }
            if((Grid[newx][newy] == lookingFor) || (Grid[newx][newy] == ' ')){
                int d = distance - 1;
                value = value + SearchOneDirection(newx, newy, dx, dy, lookingFor, d);
            }
        }
        return value;
    }

    public int[] SearchAllDirections(Character lookingFor){
        int[] v = new int[8];
        v[0] = 0; //# of spaces in BOTTOM RIGHT DIAGONAL containing the character lookingFor
        v[1] = 0; //# of spaces in RIGHT DIRECTION containing the character lookingFor
        v[2] = 0; //# of spaces in TOP RIGHT DIAGONAL containing the character lookingFor
        v[3] = 0; //# of spaces in UP DIRECTION containing the character lookingFor
        v[4] = 0; //# of spaces in TOP LEFT DIAGONAL containing the character lookingFor
        v[5] = 0; //# of spaces in LEFT DIRECTION containing the character lookingFor
        v[6] = 0; //# of spaces in BOTTOM LEFT DIAGONAL containing the character lookingFor
        v[7] = 0; //# of spaces in DOWN DIRECTION containing the character lookingFor
        v[0] = SearchOneDirection(Posn_X, Posn_Y,  1, -1,  lookingFor, 4); //These should modify the v[] by setting them = to the # of lookingFor Char found in the given direction.
        v[1] = SearchOneDirection(Posn_X, Posn_Y,  1,  0,  lookingFor, 4);
        v[2] = SearchOneDirection(Posn_X, Posn_Y,  1,  1,  lookingFor, 4);
        v[3] = SearchOneDirection(Posn_X, Posn_Y,  0,  1,  lookingFor, 4);
        v[4] = SearchOneDirection(Posn_X, Posn_Y, -1,  1,  lookingFor, 4);
        v[5] = SearchOneDirection(Posn_X, Posn_Y, -1,  0,  lookingFor, 4);
        v[6] = SearchOneDirection(Posn_X, Posn_Y, -1, -1,  lookingFor, 4);
        v[7] = SearchOneDirection(Posn_X, Posn_Y,  0, -1,  lookingFor, 4);
        return v;
    }
}