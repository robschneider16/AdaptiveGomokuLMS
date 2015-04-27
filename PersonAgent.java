import java.io.IOException;
import java.util.Random;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Iterator;
import java.util.Vector;
import java.io.StringReader;
import java.lang.String;

public class PersonAgent {
	public static final int GOMOKUPORT = 17033;
	public Socket Server_Socket;				// socket for communicating w/ server
	public BufferedReader Server_Channel_In;    // Channel to read information from server
	public PrintWriter Server_Channel_Out;      // Channel to send a move to the server
    public StringReader Game_State_Reader;
    public Hypothesis hyp;
    public Random test = new Random();
    public String Game_Outcome = "";

    public int Best_Move_X = 0;
    public Vector<Integer> Memory_Of_Past_X_Moves = new Vector<Integer>();
    public int Best_Move_Y = 0;
    public Vector<Integer> Memory_Of_Past_Y_Moves = new Vector<Integer>();
    public double Best_Move_Estimated_Value = -100000000;
    public boolean Is_Game_Over = false;
    
	public int myID;

	public int Board_Size;
    public Vector<Character[][]> Memory_Of_Grid;
	public Character[][] Grid;
    public Integer[][] GridValues;
    public Representation MyRep;
    public String Player1 = "x";
    public String Player2 = "o";
    public String Me;
    
	public String Player_Name = "Player";

	
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
        
        //System.out.println(" \t");
        for(int i = 0; i < Board_Size; i++){
            for(int p = 0; p < Board_Size; p++){
                Grid[p][i] = g.charAt(p);
            }
            //System.out.print(""+i+"\t|");
            //System.out.print(g + "|");
            //System.out.println();
            g = Server_Channel_In.readLine();
        }
        //System.out.println(g);
        if(g.equals(Player1)){
            Me = Player1;
            hyp = new Hypothesis(Me, Player2, Board_Size);
            //System.out.println("I am the black player");
        }else{
            Me = Player2;
            hyp = new Hypothesis(Me, Player1, Board_Size);
            //System.out.println("I am the white player");
        }
        
        int x = test.nextInt(Board_Size);
        int y = test.nextInt(Board_Size);
        while(Grid[x][y] == 'x' || Grid[x][y] == 'o'){
            x = test.nextInt(Board_Size);
            y = test.nextInt(Board_Size);
        }
        Best_Move_X = x;
        Best_Move_Y = y;
        Memory_Of_Grid = new Vector<Character[][]>();
        PlaceMove(x,y);
        Best_Move_Estimated_Value = -100000000;
    
        
    }
    
    
    //Gets the current board state, saves it in the 2D array of characters
    //outputs a visua representation to the terminal
    public void GetBoardState() throws IOException{
        String g = Server_Channel_In.readLine();
        //System.out.print(" \n");
        //System.out.println("Game State = "+g);
        if(g.equals("continuing") != true ){
            Is_Game_Over = true;
            System.out.println("Player " + Me + " = " + g);
            Game_Outcome = g;
        }else{
            g = Server_Channel_In.readLine();
            for(int i = 0; i < Board_Size; i++){
                for(int p = 0; p < Board_Size; p++){
                    Grid[p][i] = g.charAt(p);
                }
                //System.out.print(""+i+"\t|");
                //System.out.print(g + "|");
                g = Server_Channel_In.readLine();
                //System.out.println();
            }
        }
    }

    
    //send a move to the server, and remember the moves we did so we can learn from out past
    public void PlaceMove(int x, int y){
        Server_Channel_Out.println(x);
        Server_Channel_Out.println(y);
        //System.out.println(Me + "-Player placing piece at x = " + x + ", y = " + y );
        //Grid[]
        Memory_Of_Grid.add(Grid);//when to add the grid to the memory, and how often, Figure out later when we are doing the learning
        //might need a vector or moves to keep track of what we did.(for the learning process
        Memory_Of_Past_X_Moves.add(x);
        Memory_Of_Past_Y_Moves.add(y);
    }
    
	public static void main (String args[]) throws Exception {
		PersonAgent Rob = new PersonAgent();
        if(args.length == 0){
            Rob.ConnectToGrid("localhost",GOMOKUPORT);
        }else{
            Rob.ConnectToGrid(args[0],GOMOKUPORT);
        }
        Scanner sc = new Scanner(System.in);
        Rob.InitGame();
        //Thread.sleep(1);
        System.out.print("Type numbers for the X and Y position, and press enter. Range availible is 1 => "+ Rob.Board_Size);
        while(Rob.Is_Game_Over == false){
            Rob.GetBoardState();
            if(Rob.Is_Game_Over == false){
                System.out.print("place at X = ");
                int x = (sc.nextInt() - 1);
                System.out.println();
                System.out.print("place at Y = ");
                int y = (sc.nextInt() - 1);
                System.out.println();
                Rob.PlaceMove(y,x);
            }
            //Thread.sleep(1);
        }
        //Rob.hyp.endGame(Rob.Game_Outcome, Rob.Memory_Of_Grid, Rob.Memory_Of_Past_X_Moves, Rob.Memory_Of_Past_Y_Moves);
        //Thread.sleep(1);
        System.exit(1);
    }
	
}