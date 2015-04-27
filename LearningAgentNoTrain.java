import java.io.IOException;
import java.util.Random;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Vector;
import java.io.StringReader;
import java.lang.String;

public class LearningAgentNoTrain {
	public static final int GOMOKUPORT = 17033;
	public Socket Server_Socket;				// socket for communicating w/ server
	public BufferedReader Server_Channel_In;    // Channel to read information from server
	public PrintWriter Server_Channel_Out;      // Channel to send a move to the server
    public StringReader Game_State_Reader;
    public HypothesisN hyp;
    public Random test = new Random();
    public String Game_Outcome = "";

    public int Best_Move_X = 0;
    public int Best_Move_Y = 0;
    public double Best_Move_Estimated_Value = -100000000;
    public boolean Is_Game_Over = false;
    public int TrainingIndex = 0;

	public int Board_Size;
    public Character[][][] Memory_Of_Grid;
	public Character[][] Grid;
    public Character[][] tempGrid;
    public String Player1 = "x";
    public String Player2 = "o";
    public String Me;
    
	public String Player_Name = "Robert's Learning Machine";

	
	//connects to server, and creates connection to send and recieve data.
	public void ConnectToGrid(String name, int portnum){
		try {
			Server_Socket = new Socket(name, portnum);
			Server_Channel_Out = new PrintWriter(Server_Socket.getOutputStream(), true);
			Server_Channel_In = new BufferedReader(new InputStreamReader(Server_Socket.getInputStream()));
            //System.out.println("" + Player_Name + " conected to server.");
		} catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + name);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + name);
            System.exit(1);
        }
	}
	//gets the first board state to determine what player it is, and how big the board is.
    //assumes the board is a square shape!!!
    public void InitGame() throws IOException{
        String g = Server_Channel_In.readLine();
        //System.out.println("Game state = " + g);
        g = Server_Channel_In.readLine();
        Board_Size = g.length();
        Grid = new Character[Board_Size][Board_Size];
        tempGrid = new Character[Board_Size][Board_Size];
        
        //System.out.println(" \t");
        for(int z = 0; z < Board_Size; z++){
            for(int p = 0; p < Board_Size; p++){
                Grid[p][z] = g.charAt(p);

            }
            //System.out.print(""+i+"\t|");
            //System.out.print(g + "|");
            //System.out.println();
            g = Server_Channel_In.readLine();
        }
        int x = test.nextInt(Board_Size-4)+2;
        int y = test.nextInt(Board_Size-4)+2;
        while(Grid[x][y].equals('x') || Grid[x][y].equals('o')){
            x = test.nextInt(Board_Size-4)+2;
            y = test.nextInt(Board_Size-4)+2;
        }
        Best_Move_X = y;
        Best_Move_Y = x;
        PlaceMove();
        //System.out.println(g);
        if(g.equals(Player1)){
            Me = Player1;
            hyp = new HypothesisN(Me, Player2, Board_Size);
            //System.out.println("I am the black player");
        }else{
            Me = Player2;
            hyp = new HypothesisN(Me, Player1, Board_Size);
            //System.out.println("I am the white player");
        }
        Best_Move_Estimated_Value = -100000000;
        Memory_Of_Grid = new Character[Board_Size][Board_Size][Board_Size*Board_Size];
        for(int i=0;i<Board_Size;i++){
            for(int e=0;e<Board_Size;e++){
                Memory_Of_Grid[i][e][0] = Grid[i][e];
            }
        }
        //System.out.println(EvaluationEstimation(Grid));
    }
    
    
    //Gets the current board state, saves it in the 2D array of characters
    //outputs a visua representation to the terminal
    public void GetBoardState() throws IOException{
        String g = Server_Channel_In.readLine();
        //System.out.print(" \n");
        //System.out.println("Game State = "+g);
        if(g.equals("continuing") != true ){
            Is_Game_Over = true;
            System.out.println("Player NoTrain " + Me + " = " + g);
            Game_Outcome = g;
        }
        g = Server_Channel_In.readLine();
        for(int i = 0; i < Board_Size; i++){
            for(int p = 0; p < Board_Size; p++){
                Grid[p][i] = g.charAt(p);  
                Memory_Of_Grid[p][i][TrainingIndex] = g.charAt(p);
            }
            //System.out.print(""+i+"\t|");
            //System.out.print(g + "|");
            g = Server_Channel_In.readLine();
            //System.out.println();
        }
        TrainingIndex += 1; 
    }

    
    //send a move to the server, 
    public void PlaceMove(){
        Server_Channel_Out.println(Best_Move_X);
        Server_Channel_Out.println(Best_Move_Y);
        //System.out.println(Me + "-Player placing piece at x = " + x + ", y = " + y );
        Best_Move_Estimated_Value = 0;
        Best_Move_X = -1;
        Best_Move_Y = -1;
    }
    
    //Compute the best move to play next
    public void ComputeMove(){
        //loop through the grid compute the Estimate Value for each grid position. save the best value, and its x and y coordinates.
        Best_Move_Estimated_Value = -100000;
        boolean firstmove = true;
        double tv =0;
        double oldGridValue = EvaluationEstimation(Grid);
        for(int j = 0; j < Board_Size; j++){
            for(int i = 0; i < Board_Size; i++){
                if(Grid[i][j].equals(' ')){
                    Grid[i][j] = Me.charAt(0);
                    tv = EvaluationEstimation(Grid);
                    if((tv > Best_Move_Estimated_Value) && (tv > oldGridValue)){
                        Best_Move_X = j;
                        Best_Move_Y = i;
                        Best_Move_Estimated_Value = tv;
                        firstmove = false;
                    }
                    Grid[i][j] = ' ';
                }
            }
        }
        if(firstmove){
            int x = test.nextInt(Board_Size);
            int y = test.nextInt(Board_Size);
            while(Grid[x][y].equals('x') || Grid[x][y].equals('o')){
                x = test.nextInt(Board_Size);
                y = test.nextInt(Board_Size);
            }
            Best_Move_X = y;
            Best_Move_Y = x;
            System.out.println("played Random move So I dont forfeit");
        }
        //System.out.println(EvaluationEstimation(g));
    }
    
    
    
    //Evaluates the "Goodness" of a move/position.
    public double EvaluationEstimation(Character g[][]){
        String opponent;
        Representation Temp_Rep;
        if(Me.equals(Player1)){
            opponent = Player2;
        }else{
            opponent = Player1;
        }
        Temp_Rep = new Representation(g, Me, opponent);
        //Temp_Rep.ComputeRepresentation();
        double Value = 0;
        //Assume Hypothesis and Representation are the same length
        for(int i = 0; i < Temp_Rep.SRep.length; i++){
            Value += (hyp.Weights[i] * Temp_Rep.SRep[i]);
            //System.out.println(i + "---score" + Value + "  ---rep" +Temp_Rep.SRep[i]);
        }
        return Value;
    }

	
	public static void main (String args[]) throws Exception {
		LearningAgentNoTrain Rob = new LearningAgentNoTrain();
        if(args.length == 0){
            Rob.ConnectToGrid("localhost",GOMOKUPORT);
        }else{
            Rob.ConnectToGrid(args[0],GOMOKUPORT);
        }
        Rob.InitGame();
        Rob.GetBoardState();
        while(Rob.Is_Game_Over == false){
            Rob.ComputeMove();
            Rob.PlaceMove();
            Rob.GetBoardState();
        }
        //Rob.GetBoardState();
        Character[][][] gm = new Character[Rob.Board_Size][Rob.Board_Size][Rob.TrainingIndex];
        for(int ti = 0; ti <Rob.TrainingIndex; ti++){
            for(int i = 0; i < Rob.Board_Size; i++){
                for(int j = 0; j < Rob.Board_Size; j++){
                    gm[j][i][ti] = Rob.Memory_Of_Grid[j][i][ti];
                }
            }
        }
        //Rob.hyp.endGame(Rob.Game_Outcome, gm, Rob.TrainingIndex);
        /*
        for(int ti = 0; ti <Rob.TrainingIndex; ti++){
            for(int i = 0; i < Rob.Board_Size; i++){
                System.out.print(""+i+"\t|");
                for(int j = 0; j < Rob.Board_Size; j++){
                    Character g =  Rob.hyp.Memory_Of_Grid[i][j][ti];
                    
                
                    System.out.print(g);
                }
                System.out.print("|");
            System.out.println();
            }
            System.out.println("--------------------------------------------");
        }*/
        //System.out.println(Rob.hyp.DebugTestString);
        System.exit(1);
    }
	
}