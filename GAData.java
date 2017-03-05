 

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


/**
 * GAData stores the data associated with the genetic algorithm, such as the array of elite designs. It provides
 * a number of methods to allow threads to get and set such data in a threadsafe manner.
 * 
 * 
 * @author 620018133 & 630019424
 * @version 1.0
 */
public class GAData
{
    private int populationSize;
    private int eliteSize;
    private double crossoverRate;
    private double mutationRate;
    private int functionEvaluations;
    private Problem genericProblem;
    private volatile boolean termination = false;
    private volatile int counter = 0;
    private volatile boolean eliteSetLock = false;
    private volatile boolean eliteCopySetLock= false;
    private Random r = new Random();
    private volatile ArrayList<Design> designs = new ArrayList<Design>();
    private volatile ArrayList<Design> eliteDesigns = new ArrayList<Design>();
    private volatile ArrayList<Design> eliteDesignsCopy = new ArrayList<Design>();
    
    GAData(int p, int e, double c, double m, int f, Problem g) {
        this.populationSize = p;
        this.eliteSize = e;
        this.crossoverRate = c;
        this.mutationRate = m;
        this.functionEvaluations = f;
        this.genericProblem = g;
    }
    
    /**
     * Synchronized method to add design into designs array. 
     * 
     * @param d Design to be added into array
     */
    public synchronized void addDesign(Design d){
        designs.add(d);
    }

    /**
     * Synchronized method to add design into the elite designs array if it qualifies as an
     * elite design. This method returns false if the elite designs array is currently inaccessible 
     * (as the lock is held) or if the design passed in has not yet been evaluated. The method returns
     * true when the design has been checked, and added into the elite designs array (if necessary).
     * 
     * @param d Design to be added into the elite design array
     * @returns boolean if the elite designs array has been accessed
     */
    public synchronized boolean addEliteDesign(Design d){ 
        // If the elite designs array is currently inaccessible, return false;
        if (this.eliteSetLock == true){
            return false;
        }

        // Set the lock to true, so other threads cannot access the array until
        // the lock is released
        this.eliteSetLock = true;

        Design minimum;

        // If the design passed in has not been evaluated, print error message and
        // exit the system
        if(d.isEvaluated() == false){
            MultiThreadedGeneticAlgorithm.error("Design has not been evaluated");
        }

        // If the number of elite designs is still smaller than the required group
        // size, add the design as an elite design
        if(this.eliteDesigns.size() < eliteSize){
            eliteDesigns.add(d);
            // Update the copy array of elite designs
            this.eliteDesignsCopy = this.eliteDesigns;
            // Release the lock
            this.eliteSetLock = false;
            return true;
        }

        // Get the value of the current worst design in the elite designs array
        minimum = Collections.min(this.eliteDesigns);

        // Now the elite designs array is full, check if the value of the design passed in
        // is greater than the value of the worst design in the elite array
        if(d.getValue().doubleValue() > minimum.getValue().doubleValue()){
            // Swap the design with the current worst design in the array
            eliteDesigns.set(eliteDesigns.indexOf( (Object) minimum), d);
            // Update the copy array of elite designs
            this.eliteDesignsCopy = this.eliteDesigns;
            // Realease the lock
            this.eliteSetLock = false;
            return true;
        }
        
        // Else the design does not qualify as an elite design, but as it has been checked
        // we release the lock and return true
        this.eliteSetLock = false;
        return true;
    }
    
    /**
     * Synchronized method to get the size of the designs array.
     * 
     * @returns size of design array
     */
    public synchronized int getDesignsArraySize(){
        return this.designs.size();
    }
    
    /**
     * Synchronized method to get the size of the elite designs array.
     * 
     * @returns size of the elite designs array
     */
    public synchronized int getEliteDesignsArraySize(){
        return this.eliteDesigns.size();
    }
   
    /**
     * Method to return the current elite designs array.
     * 
     * @returns ArrayList<Design> array of elite designs
     */
    public ArrayList<Design> getEliteDesignsArray(){
        return this.eliteDesigns;
    }

    /**
     * Synchronized method which gets the first design in the designs array and removes
     * it from the array.
     * 
     * @returns Design first design in the elite design array
     */
    public synchronized Design getAndRemoveDesign(){
        Design temp = this.designs.get(0);
        this.designs.remove(temp);
        return temp;
    }

    /**
     * Synchronized method which returns a random design from the elite designs array.
     * If a lock is held on the elite designs array, a most recent copy of the array is 
     * attempted to be accessed. If a lock is also held on the copy array, null is returned.
     * 
     * @returns Design random design from elite desings array or from its most recent copy array
     */
    public synchronized Design getRandomEliteDesign(){
        Design temp;
        // Check main elite designs array is not locked
        if(this.eliteSetLock == false){
            // Set lock
            this.eliteSetLock = true;
            // Get random design in the array
            temp = this.eliteDesigns.get(r.nextInt(eliteDesigns.size()));
            // Release lock
            this.eliteSetLock = false;
            return temp;
        } // Else check the copy array is not locked
        else if(this.eliteCopySetLock == false){
            // Set lock
            this.eliteCopySetLock = true;
            // Get random design in the array
            temp = this.eliteDesignsCopy.get(r.nextInt(eliteDesignsCopy.size()));
            // Release lock
            this.eliteCopySetLock = false;
            return temp;
        }
        // If both arrays are locked, return null
        return null;
    }

    /**
     * Synchronized method which checks the flag that signifies if the 
     * termination criteria has been met.
     * 
     * @returns boolean the termination flag
     */
    public synchronized boolean terminationMet(){
        return termination;
    }

    /**
     * Synchronized method which checks is the number of evalutations has been met.
     * If it has been the flag is set to true and the method also returns true.
     * 
     * @returns boolean if required number of evaluations has been met
     */
    public synchronized boolean checkAndSetTermination(){
        // Check if the counter is equal to the number of function evaluations
        if(this.counter == this.functionEvaluations){
            // Set flag
            termination = true;
            return true;
        }
        return false;
    }
    
    /**
     * Synchronized method which increments the counter that keeps track of the number of
     * evaluations completed so far, and checks whether the required number of evaluations 
     * has been met. If they have the method returns true.
     * 
     * @returns boolean if the required number of evaluations has been met
     */
    public synchronized boolean incrementCounter(){
        this.counter++;
        return this.checkAndSetTermination();
    }

    /**
     * Synchronized method which returns the state of elite set lock. If the elite set is locked
     * this will return true.
     * 
     * @returns boolean if the elite set is locked
     */
    public synchronized boolean checkEliteSetLock(){
        return this.eliteSetLock;
    }
    
    /**
     * Method which returns the problem stored in this class.
     * 
     * @returns Problem stored in this class
     */
    public Problem getProblem(){
        return this.genericProblem;
    }
}
