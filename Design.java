package ga;

import java.util.ArrayList;
import java.util.Random;
import java.io.Serializable;

/**
 * The Design class encapsulates a representation of a solution to a Problem, and provides 
 * methods to compare to other Designs
 * 
 * @author Jonathan Fieldsend 
 * @version 1.3
 */
public class Design implements Comparable<Design>, Serializable
{
    // members for comparison and random number generation
    private static final int SMALLER = -1;
    private static final int EQUAL = 0;
    private static final int BIGGER = 1;
    private static final Random rng = new Random();
    // state members
    private Number value; // value (quality) of this design
    private Problem problem; // problem being solved
    private ArrayList<Boolean> designVector; // solution representation
    
    /**
     * Constructs this design initially with a random solution for the given Problem argument
     * 
     * @param problem Problem that this design will be tackling
     */
    Design(Problem problem){
        this.problem = problem;
        this.designVector = problem.getRandomDesignVector();
    }

    /**
     * Evaluates this design on its problem
     */
    synchronized void evaluate() {
        if (this.value == null)
            this.value = this.problem.evaluate(this);
    }

    /** {@InheritDoc}
     */
    @Override
    public int compareTo(Design otherDesign){
        if (this == otherDesign)
            return Design.EQUAL;
        if (this.equals(otherDesign)) //need to ensure consistency with equals  
            return Design.EQUAL; 
        if (this.value.doubleValue() < otherDesign.value.doubleValue())
            return Design.SMALLER;
        else if (this.value.doubleValue() > otherDesign.value.doubleValue())
            return Design.BIGGER;
        return Design.EQUAL;       
    }

    /** {@InheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (obj instanceof Design)
            if (this.designVector.equals(((Design) obj).designVector))
                return true;

        return false;    
    }

    /** {@InheritDoc}
     */
    @Override
    public int hashCode(){
        return designVector.hashCode();
    }
    
    /**
     * Method returns whether this design stored in this Design has been evaluated yet
     * 
     * @return true if evaluated, false otherwise
     */
    boolean isEvaluated() {
        return value != null;
    }

    /**
     * Method modifies the design stored in this Design be evolving it using another Design
     * with the provided crossover probability, and then mutates elements of the child 
     * produced with the mutation probability. This child will replace the design held in this 
     * Design.
     * 
     * @param otherDesign design to use as the other parent alsongside this design
     * @param crossoverProb probability of crossing-over an element from the otherDesign, 
     * must be on the range [0,1]
     * @param crossoverProb probability of mutating an element from this design, 
     * must be on the range [0,1]
     */
    void evolve(Design otherDesign, double crossoverProb, double mutationProb) {
        this.crossover(otherDesign, crossoverProb);
        this.mutate(mutationProb);
        this.value = null;
    }
    
    /**
     * Method returns a Boolean array containing the design parameters of this
     * design
     * 
     * @returns array containing the design parameters of this design
     */
    public Boolean[] getDesignParameters() {
        return this.designVector.toArray(new Boolean[this.designVector.size()]);
    }
    
    /**
     * Method returns the value of this design, or null if it has not 
     * yet been evaluated
     * 
     * @returns the value of this design, or null if this design has not been 
     * evaluated
     */
    public Number getValue() {
        return this.value;
    }
    
    /*
     * Method crosses over this design with the otherDesign with crossoverProb probability
     * for each element (uses Uniform Crossover)
     */
    private void crossover(Design otherDesign, double crossoverProb){
        for (int i=0; i < this.designVector.size(); i++){
            if (this.rng.nextDouble() < crossoverProb){
                this.designVector.set(i, otherDesign.designVector.get(i));
            }
        }
    }

    /*
     * Method mutates this design with mutationProb probability (uses bit flip mutation)
     */
    private void mutate(double mutationProb){
        for (int i=0; i < this.designVector.size(); i++){
            if (this.rng.nextDouble() < mutationProb){
                if (this.designVector.get(i)) {
                    this.designVector.set(i, false);
                } else {
                    this.designVector.set(i, true);
                }
            }
        }
    }
}
