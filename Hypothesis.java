import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
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

public class Hypothesis {
    public double[] Weights = new double[45];
    public String OldWeights = "";
    public Character[][][] Memory_Of_Grid;
    public int[] Memory_of_X_Moves;
    public int[] Memory_of_Y_Moves;
    public String Opponent;
    public String Me;
    public String Player1 = "x";
    public String Player2 = "o";
    public int Board_Size;
    public String DebugTestString = "";
    public double n = 0.0001;

    //Creates a Hypothesis, read and set weights from last file
    public Hypothesis(String player, String opp, int size){
        Me = player;
        Opponent = opp;
        Board_Size = size;
        readSavedData();
    }

    //Called when the game is over to train, modify and save the hypothesis
    public void endGame(String game, Character mg[][][], int numofturns, int mx[], int my[]){
        Memory_Of_Grid = mg;
        Memory_of_X_Moves = mx;
        Memory_of_Y_Moves = my;
        String temp = "";
        double Vb = 0;

        if(game.equals("win")){
            Vb = 100;
        }
        if(game.equals("draw")){
            Vb = 0;
        }
        if(game.equals("lose")){
            Vb = -100;
        }
        vTrain(Vb, numofturns-1);
        //creates a string of the weights to save, separated by spaces.
        temp = String.valueOf(Weights[0]);
        for(int i = 1; i < Weights.length; i++){
            temp += " ";
            temp += String.valueOf(Weights[i]);
        }
        write("Current_Data", temp);
    }

    public void vTrain(double vSuccessor, int training_index){
        //Modify the weights using LMS (DOES 2 TRAINING MODIFICATIONS EACH TIME V-TRAIN IS CALLED)
        double vb = 0;
        double vbtwo = 0;
        Character[][] grid = new Character[Board_Size][Board_Size];
        if(training_index > 0){
            //recreates the board
            for(int i = 0; i < Board_Size; i++){
                for(int j = 0; j < Board_Size; j++){
                    grid[i][j] = Memory_Of_Grid[i][j][training_index];
                }
            }
            //Evaluate the board state
            vb = EvaluationEstimation(grid);
            Representation Temp_Rep = new Representation(grid, Me, Opponent);
            //modify the weights
            for(int i = 0; i < Weights.length; i++){
                    Weights[i] = (Weights[i] + (n*(vSuccessor - vb) * Temp_Rep.SRep[i]));//lms, n = global, posative weights become more posative
            }
            vb = EvaluationEstimation(grid);


            //DebugTestString = DebugTestString + "Rep " + grid[f][d] + " with ' ' " + "\n";
            int f = Memory_of_Y_Moves[training_index-1];
            int d = Memory_of_X_Moves[training_index-1];
            //DebugTestString = DebugTestString + "Rep " + grid[f][d] + " with ' ' " + "\n";
            if(grid[f][d].equals(Me.charAt(0))){
                grid[f][d] = ' ';
                vbtwo = EvaluationEstimation(grid);
                Temp_Rep = new Representation(grid, Me, Opponent);
                for(int i = 0; i < Weights.length; i++){
                        Weights[i] = (Weights[i] + (n*(vSuccessor - vb) * Temp_Rep.SRep[i]));//lms, n = global
                }
                vb = EvaluationEstimation(grid);  
            }          
            vTrain(vb, training_index-1);
        }
    }

    //Evaluates the "Goodness" of a board state
    public double EvaluationEstimation(Character g[][]){
        Representation Temp_Rep = new Representation(g, Me, Opponent);
        double Value = 0;
        //Assume Hypothesis and Representation are the same length
        for(int i = 0; i < Weights.length; i++){
            Value += (Weights[i] * Temp_Rep.SRep[i]);
        }
        return Value;
    }

    //writes the weights to file.
    public void write(String name, String content){
        try{
            File file = new File("HypothesisData/" + Me + "/" + name + ".txt");
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void readSavedData(){
        try{
            FileReader a = new FileReader("HypothesisData/"+ Me + "/Current_Data.txt");
            //FileReader n = new FileReader("HypothesisData/"+ Me + "/Old_Data.txt");

            BufferedReader bufferedText = new BufferedReader(a);
            //BufferedReader oldText = new BufferedReader(n);
            //int p = oldText.read();
            //while(p != -1){
              //  char c = (char)p;
                //String x = String.valueOf(c);
                //OldWeights = OldWeights + x;
                //p = oldText.read();
            //}
            int q = bufferedText.read();
            int i = 0;
            double x;
            String temp = "";
            while(q != -1){
                Character c = (char)q;
                if(c.equals(' ')){
                    x = Double.valueOf(temp);
                    Weights[i] = x;
                    i = i + 1;    
                    temp = "";
                }else{
                    temp = temp + String.valueOf(c);
                }
                q = bufferedText.read();
            }
            x = Double.valueOf(temp);
            Weights[i] = x;
            
            a.close();
            //n.close();
            //oldText.close();
            bufferedText.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}