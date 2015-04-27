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

public class Hypothesisp {
    public double[] Weights = new double[27];
//public int NumOfGamesPlayed;
    public String OldWeights = "";
    public Vector<Integer> Memory_Of_Past_Y_Moves;
    public Vector<Integer> Memory_Of_Past_X_Moves;
    public Vector<Character[][]> Memory_Of_Grid;
    public String Opponent;
    public String Me;
    public String Player1 = "x";
    public String Player2 = "o";
    public double n = 0.001;
//26 Weights + 1 W0, 27 in totaly 
    public Hypothesisp(String player, String opp){
    //read and set weights from last file.
        Me = player;
        Opponent = opp;
        readSavedData();
    }

    public void endGame(String game, Vector<Character[][]> mg , Vector<Integer> mx, Vector<Integer> my ){
        Memory_Of_Grid = mg;
        Memory_Of_Past_X_Moves = my;
        Memory_Of_Past_Y_Moves = mx;
        String temp = "";
    //for(int i = 0; i < Weights.length; i++){
      //  temp = temp + String.valueOf(Weights[i]);
      //  temp =temp + " ";
    //}
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
        vTrain(Vb, Memory_Of_Grid.size()-1);
        temp = String.valueOf(Weights[0]);
        for(int i = 1; i < Weights.length; i++){
            temp += " ";
            temp += String.valueOf(Weights[i]);
        }
        write("Current_Data", temp);
    }


    public void vTrain(double vSuccessor, int training_index){
    //Modify the weights using LMS
        if(training_index > 0){
            Character[][] grid = Memory_Of_Grid.get(training_index);
            int lx = Memory_Of_Past_X_Moves.get(training_index);
            int ly = Memory_Of_Past_Y_Moves.get(training_index);
            double vb = EvaluationEstimation(lx, ly);
            Representationp Temp_Rep = new Representationp(lx, ly, grid, Me, Opponent);
            Temp_Rep.ComputeRepresentation();
            for(int i = 0; i < Weights.length; i++){
                Weights[i] = (Weights[i] + (n * (vSuccessor - vb) * Temp_Rep.SRep[i]));
            }
        //Memory_Of_Grid.remove(training_index);
         vb = EvaluationEstimation(lx, ly);//do i do this or not
         vTrain(vb, training_index-1);
     }
 }

//Evaluates the "Goodness" of a move/position.
 public double EvaluationEstimation(int x, int y){
    String opponent;
    Representationp Temp_Rep;
    if(Me == Player1){
        opponent = Player2;
    }else{
        opponent = Player1;
    }
    int lastgrid = Memory_Of_Grid.size() - 1;
    Temp_Rep = new Representationp(x, y, Memory_Of_Grid.get(lastgrid), Me, Opponent);
    Temp_Rep.ComputeRepresentation();
    double Value = 0;
    //Assume Hypothesis and Representation are the same length
    for(int i = 0; i < Weights.length; i++){
        Value = Value + (Weights[i] * Temp_Rep.SRep[i]);
    }
    return Value;
}
public void write(String name, String content){
    try{
        File file = new File("HypothesisDatap/" + Me + "/" + name + ".txt");
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

        FileReader a = new FileReader("HypothesisDatap/"+ Me + "/Current_Data.txt");
        FileReader n = new FileReader("HypothesisDatap/"+ Me + "/Old_Data.txt");

        BufferedReader bufferedText = new BufferedReader(a);
        BufferedReader oldText = new BufferedReader(n);
        int p = oldText.read();
        while(p != -1){
            char c = (char)p;
            String x = String.valueOf(c);
            OldWeights = OldWeights + x;
            p = oldText.read();
        }

        int q = bufferedText.read();
        int i = 0;
        double x;
        String temp = "";
        while(q != -1){
            char c = (char)q;
            if(c == ' '){
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