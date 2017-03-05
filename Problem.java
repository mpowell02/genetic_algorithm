package ga;

import java.util.ArrayList;

/**
 * Any design problem to be solved by the MultiThreadedGeneticAlgorithm
 * must implement this interface. Additionally, all implementors must provide a
 * no-argument constructor.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public interface Problem 
{
    /**
     * Method to enable the evaluation of the given Design on this problem
     * 
     * @param  d   A Design containing the design to evaluate on this problem
     * @return a Number containing the corresponding evaluation
     */
    Number evaluate(Design d); 
    
    /**
     * Method to generate a random boolean vector (representing a binary string) 
     * of the appropriate length to be used by this problem
     * 
     * @return a random design vector
     */
    ArrayList<Boolean> getRandomDesignVector();
}
