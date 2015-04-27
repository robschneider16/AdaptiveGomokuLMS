import java.util.Vector;

public class Representation {
    
    public int Posn_X, Posn_Y;
    public Character Me;
    public Character Opponent;
    public Character[][] Grid;
    public int boardSize;
    public int[] SRep = new int[45];
    
    //takes a game board, and creates an array of features. 45 features, 22 for each player, and 1 static .
    public Representation(Character[][] g, String player, String opp){
        Grid = g;
        Opponent = opp.charAt(0);
        Me = player.charAt(0);
        boardSize = g.length;
        ComputeRepresentation();
    }
    
    //determines what features are present on the board
    public void ComputeRepresentation(){
        //initialize SRep;
        SRep[0]= 1; //no vaiable, w0, static
        for(int i = 1; i < SRep.length; i++){
            SRep[i] = 0;
        }
        for(int v = 0; v < boardSize; v++){
            //search through vertical directions of grid
            String s = GetStringFromGridDirection(v,0,0,1);//one row of the grid in a complete string.
            AddToReps(s);
            //search through horizontal directions
            s = GetStringFromGridDirection(0,v,1,0);//one row of the grid in a complete string.
            AddToReps(s);
            //search through both diagonal directions
            s = GetStringFromGridDirection(v,0,1, 1);//one row of the grid in a complete string.
            AddToReps(s);
            s = GetStringFromGridDirection(0,v,1, 1);//one row of the grid in a complete string.
            AddToReps(s);
            //if v=0, then the following directions have already been explored from above functions. (overlap)
            if(v != 0 ){
                s = GetStringFromGridDirection(v,0, -1,1);//one row of the grid in a complete string.
                AddToReps(s);
                s = GetStringFromGridDirection(boardSize-1, v, -1,1);//one row of the grid in a complete string.
                AddToReps(s);
            }
        }
    }


    //creates a string from the gameboard. the string represents one diagonal,vertical,horizontal column of the board.
    public String GetStringFromGridDirection(int x, int y, int dx, int dy){
        String va = "" + Grid[x][y];
        int newx = x;
        int newy = y;
        boolean looking = true;
            while(looking == true){
                newx += dx;
                newy += dy;
                if((newy >= boardSize) || (newx >= boardSize) || (newx < 0) || (newy < 0)){
                    looking = false;
                    break;
                }else{
                    va = va + Grid[newx][newy];
                }
            }
        return va;
    }

    public void AddToReps(String str){//edits the representation by evaluating the given string,
        //My Representation Values
        String x = ""+Me;
        String o = ""+Opponent;
        String oo = o;
        String s = " ";
        //SRep[0]  += Contains(str, s+s+s+s+s+s);
        SRep[1]  +=(Contains(str, s+x+x+x+x)- Contains(str, s+x+x+x+x+s));
        SRep[2]  +=(Contains(str, x+x+x+x+s) - Contains(str, s+x+x+x+x+s));
        SRep[3]  += Contains(str, s+x+x+x+x+s);
        SRep[4]  += Contains(str, o+x+x+x+s+s);
        SRep[5]  += Contains(str, s+x+x+s+x+s);
        SRep[6]  += Contains(str, s+x+s+x+x+s);
        SRep[7]  += Contains(str, o+x+x+s+s+s);
        SRep[8]  += Contains(str, s+s+s+x+x+o);
        SRep[9]  += Contains(str, o+x+s+s+s+s);
        SRep[10] += Contains(str, s+s+s+s+x+o);
        SRep[11] += Contains(str, o+x+s+x+s+s);
        SRep[12] += Contains(str, s+s+x+s+x+o);
        SRep[13] += Contains(str, o+s+s+x+s+x);
        SRep[14] += Contains(str, x+s+x+s+s+o);
        SRep[15] += Contains(str, x+s+x+x+x);
        SRep[16] += Contains(str, x+x+x+s+x);
        SRep[17] += Contains(str, x+x+s+x+x);
        SRep[18] += Contains(str, s+x+x+x+s);
        SRep[19] += Contains(str, s+x+s+x+s);
        SRep[20] += Contains(str, x+s+x+s+x);
        SRep[21] += Contains(str, s+s+x+x+x+o);
        SRep[22] += Contains(str, x+x+x+x+x);

        o = x;//switch the opponent and me characters so i dont have to switch all the variables in the representation
        x = oo;
        //Opponets Representation Values
        SRep[23]  +=(Contains(str, s+x+x+x+x)- Contains(str, s+x+x+x+x+s));
        SRep[24]  +=(Contains(str, x+x+x+x+s) - Contains(str, s+x+x+x+x+s));
        SRep[25]  += Contains(str, s+x+x+x+x+s);
        SRep[26]  += Contains(str, o+x+x+x+s+s);
        SRep[27]  += Contains(str, s+x+x+s+x+s);
        SRep[28]  += Contains(str, s+x+s+x+x+s);
        SRep[29]  += Contains(str, o+x+x+s+s+s);
        SRep[30]  += Contains(str, s+s+s+x+x+o);
        SRep[31]  += Contains(str, o+x+s+s+s+s);
        SRep[32] += Contains(str, s+s+s+s+x+o);
        SRep[33] += Contains(str, o+x+s+x+s+s);
        SRep[34] += Contains(str, s+s+x+s+x+o);
        SRep[35] += Contains(str, o+s+s+x+s+x);
        SRep[36] += Contains(str, x+s+x+s+s+o);
        SRep[37] += Contains(str, x+s+x+x+x);
        SRep[38] += Contains(str, x+x+x+s+x);
        SRep[39] += Contains(str, x+x+s+x+x);
        SRep[40] += Contains(str, s+x+x+x+s);
        SRep[41] += Contains(str, s+x+s+x+s);
        SRep[42] += Contains(str, x+s+x+s+x);
        SRep[43] += Contains(str, s+s+x+x+x+o);
        SRep[44] += Contains(str, x+x+x+x+x);

    }

    //returns a number of how many times the 'lookingfor' string is found in the string 's'.
    public int Contains(String s, String lookingfor){
        int l = lookingfor.length()-1;
        int value = 0;
        String lm = "";
        for(int i = 0; i < (s.length()-l); i++){
            lm = "";
            int h = i;
            while(lm.length() <= l){
                lm = lm + s.charAt(h);
                h = h + 1;
            }
            if(lm.equals(lookingfor)){
                value++;
            }
        }
        return value;
    }
    
    //for testing
    /*
    public static void main(String args[]){
        Character[][] Gd  ={{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                            {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                            {' ', ' ', ' ', 'o', ' ', ' ', ' ', ' ', ' '},
                            {' ', ' ', 'o', ' ', ' ', ' ', ' ', ' ', ' '},
                            {' ', 'o', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                            {'o', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}};
        Representation rob = new Representation(Gd, "x", "o");
        for(int i = 0; i < rob.SRep.length; i++){
            System.out.println(" SREP" + i + " = "+rob.SRep[i]);
        }
        //System.out.println(rob.Contains("xxx         ", "xxxx"));
    }
  */

}