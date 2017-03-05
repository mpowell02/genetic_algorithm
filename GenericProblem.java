import java.util.ArrayList;
import java.util.Random;
import java.io.Serializable;
import ga.*;

/**
 * Write a description of class EngineProblem here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class GenericProblem implements Problem, Serializable
{

    public GenericProblem(){
    }

    public Number evaluate(Design d){
        // set sum of true and false entries = 1, so if there are neither we do no divide by 0
        double trueSum = 1;
        double falseSum = 1;
        // sum the number of true and false values in the design vector
        // a higher ratio of true to false means a better solution
        for (boolean b: d.getDesignParameters()){
            if (b == true){
                trueSum += 1;
            } else
                falseSum += 1;
        }
        double ratio = 1/(trueSum+falseSum);
        return ratio*trueSum;
    }

    // Method which returns an random sized list of booleans
    public ArrayList<Boolean> getRandomDesignVector(){
        ArrayList<Boolean> list = new ArrayList<Boolean>();
        Random r = new Random();
        // Set the number of booleans in array == size of population?
        int a = r.nextInt(10);

        while (a==1 || a==0){
            a = r.nextInt(10);
        }
        
        // set design vector of legnth 10 for testing purposes
        // all design vectors must be the same length, else we get a index out of bounds exception when trying to evaluate.
        for (int i = 0; i < 100; i++){
            list.add(r.nextBoolean());
        }
        return list;
    }
}
