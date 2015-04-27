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

public class HypothesisN {
    public double[] Weights = new double[45];
    //public int NumOfGamesPlayed;
    public String OldWeights = "";
    public Character[][][] Memory_Of_Grid;
    public String Opponent;
    public String Me;
    public String Player1 = "x";
    public String Player2 = "o";
    public int Board_Size;
    public String DebugTestString = "";
    public double n = 0.01;
    //42 Weights + 1 W0, 43 in totaly 
    public HypothesisN(String player, String opp, int size){
    //read and set weights from last file.
        Me = player;
        Opponent = opp;
        readSavedData();
        Board_Size = size;
    }

    public void endGame(String game, Character mg[][][], int numofturns){
        Memory_Of_Grid = mg;
        String temp = "";
        for(int i = 0; i < Weights.length; i++){
            temp = temp + String.valueOf(Weights[i]);
            temp =temp + " ";
        }
        //OldWeights = OldWeights + temp;
        //write("Old_Data", OldWeights);
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
        vTrain(Vb, numofturns-1);//might need -2 if its a draw. not sure. see what it sends on a draw.
        temp = String.valueOf(Weights[0]);
        for(int i = 1; i < Weights.length; i++){
            temp += " ";
            temp += String.valueOf(Weights[i]);
        }
        write("Current_Data", temp);
    }

    public void vTrain(double vSuccessor, int training_index){
        //Modify the weights using LMS
        double vb = 0;
        Character[][] grid = new Character[Board_Size][Board_Size];
        if(training_index > 0){
            DebugTestString = DebugTestString + " ? Got to index "+ training_index + "|";
            for(int i = 0; i < Board_Size; i++){
                for(int j = 0; j < Board_Size; j++){
                    grid[i][j] = Memory_Of_Grid[i][j][training_index];
                }
            }
            vb = EvaluationEstimation(grid);
            Representation Temp_Rep = new Representation(grid, Me, Opponent);
            for(int i = 0; i < Weights.length; i++){
                if(i != 22){
                    Weights[i] = (Weights[i] + (n*((vSuccessor-vb) * Temp_Rep.SRep[i])));
                }
                DebugTestString = DebugTestString + "Rep " + i + "= " + Temp_Rep.SRep[i] + "\n";
            }
            //Memory_Of_Grid.remove(training_index);
            vb = EvaluationEstimation(grid);            
            vTrain(vb, training_index-1);
        }
    }

    //Evaluates the "Goodness" of a board state
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
        for(int i = 0; i < Weights.length; i++){
            Value += (Weights[i] * Temp_Rep.SRep[i]);
            //System.out.println(i + "---score" + Value + "  ---rep" +Temp_Rep.SRep[i]);
        }
        return Value;
    }

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
            FileReader a = new FileReader("HypothesisDataN/"+ Me + "/Current_Data.txt");
            FileReader n = new FileReader("HypothesisDataN/"+ Me + "/Old_Data.txt");

            BufferedReader bufferedText = new BufferedReader(a);
            BufferedReader oldText = new BufferedReader(n);
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
            n.close();
            oldText.close();
            bufferedText.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}