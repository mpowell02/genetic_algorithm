 

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.ArrayList;
import java.io.*;

/**
 * MultiThreadedGeneticAlgorithm implements a multi threaded version of the genetic algorithm. Each thread manages a design
 * which is evaluated, and if good enough, added into a group of elite design solutions. A random design is then drawn from
 * this elite group and the design held by the thread is crossed over with this design to create a new design. The process
 * is repeated until the specified number of overall evaluations has been met, at which point the values of the elite 
 * designs are printed to a txt file (ordered best to worst) and the array holding the elite designs is serialized.
 * 
 * @author Matt Powell
 * @version 1.0
 */
public class MultiThreadedGeneticAlgorithm implements Serializable
{
    private static GAData data;
    private static int populationSize;
    private static int eliteSize;
    private static double crossoverRate;
    private static double mutationRate;
    private static int functionEvaluations;

    public static void main (String[] args){
        Design d;
        // Check that we have the correct number of arguments being passed in
        if (args.length != 6){
            MultiThreadedGeneticAlgorithm.error("Please enter the problem name, population number, elitist set size, "
                + "crossover probability, mutation probability, and the number of evaluations.");  
        }

        // Check the arguments are of the correct type
        populationSize = MultiThreadedGeneticAlgorithm.checkInt(args, 1);
        eliteSize = MultiThreadedGeneticAlgorithm.checkInt(args, 2);
        crossoverRate = MultiThreadedGeneticAlgorithm.checkDouble(args, 3);
        mutationRate = MultiThreadedGeneticAlgorithm.checkDouble(args, 4);
        functionEvaluations = MultiThreadedGeneticAlgorithm.checkInt(args, 5);

        // Check arguments are in the defined legal range
        if(populationSize < 1){
            MultiThreadedGeneticAlgorithm.error("The population size must be greater than or equal to 1.");
        }

        if(eliteSize < 1){
            MultiThreadedGeneticAlgorithm.error("The size of the elitist group must be greater than or equal to 1.");
        }

        if(crossoverRate < 0 || crossoverRate > 1){
            MultiThreadedGeneticAlgorithm.error("The crossover rate must be on the range [0,1].");
        }

        if(mutationRate < 0 || mutationRate > 1){
            MultiThreadedGeneticAlgorithm.error("The mutation rate must be on the range [0,1].");
        }

        if(functionEvaluations < eliteSize){
            MultiThreadedGeneticAlgorithm.error("The number of problem evaluations to be run must be greater than or equal to the size of the elitist group.");
        }

        // Try to create and instantiate new Problem class as defined by args[0]
        try {
            Problem genericProblem = Helper.getProblem(args[0]);
            if(genericProblem == null){
                throw new GAInitiationException("Problem has not been instantiated.");
            }
            // Instantiate a GAdata class with the corresponding details
            data = new GAData(populationSize, eliteSize, crossoverRate, mutationRate, functionEvaluations, genericProblem);
        } catch(GAInitiationException e){
            // If an exception is caught, print error message and exit the system
            System.out.println(e);
            System.exit(1);
        } 

        // Instantiate appropriate number of design solutions
        // Add these soutions into the Design array, stored in the data class
        try {
            for (int i = 0; i < populationSize; i++){
                d = new Design(data.getProblem());
                data.addDesign(d);
            }
        } catch (OutOfMemoryError e){
            // If the system runs out of memory, print error and exit system
            MultiThreadedGeneticAlgorithm.error("Please enter a smaller integer for the population size. The system is out of memory.");
        }

        // Start threads
        (new MultiThreadedGeneticAlgorithm()).runAlgorithm();

    }

    /**
     * Method to check whether a specified entry in a string array is an int.
     * If so, it returns the int value of this entry. If not, it exits the system, printing the error.
     * 
     * @param args string array which contains entry to be checked
     * @param index index of entry to be checked
     * @returns int value of specified entry 
     */
    private static int checkInt(String[] args, int index){
        try{
            // Try and pass the entry to an int
            int n = Integer.parseInt(args[index]);
            return n;
        }
        // If a NumberFormatException is caught, exit the system printing the error
        catch(NumberFormatException e){
            MultiThreadedGeneticAlgorithm.error(args[index] + " is not an integer.");
        }
        return 0;
    }

    /**
     * Method to check whether a specified entry in a string is a double.
     * If so, it returns the double value of this entry. If not, it exits the system, printing
     * the error.
     * 
     * @param args string array which contains entry to be checked
     * @param index index of entry to be checked
     * @returns double value of specified entry
     */
    private static double checkDouble(String[] args, int index){
        try{
            // Try and pass the entry to a double
            double n = Double.parseDouble(args[index]);
            return n;
        }
        // If a NumberFormatException is caught, exit the system printing the error
        catch(NumberFormatException e){
            MultiThreadedGeneticAlgorithm.error(args[index] + " is not a double.");
            System.exit(1);
        }
        return 0;
    }

    /**
     * Method to print error message and exit the system safely.
     * 
     * @param s string to be printed to terminal
     */
    static void error(String s) {
        System.out.println(s);
        System.exit(1);
    }

    /**
     * Method which starts the genetic algorithm by starting the appropriate
     * number of Member threads. It also starts a Sorter thread which is concerned
     * with writing the results of the algorithm.
     */
    private void runAlgorithm(){
        // Instantiate the number of threads as defined by the population size
        for (int i = 0; i < populationSize; i++){
            (new Member(i)).start();
        }
        // Start a single Sorter thread
        (new Sorter()).start();
    }

    /**
     * Method returns a deep copy of the Design passed in. That is, it creates
     * a copy of the design but with a new memory reference. This is done using 
     * serialization. If a deep copy cannot be made, an error message is printed and the
     * system is exited.
     * 
     * @param d Design to be copied
     * @returns Design copy which has a seperate memory reference
     */
    private static Design designDeepCopy(Design d){
        try{
            // Serialize the Design
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
            objectOut.writeObject(d);
            // Read in as new object and cast to type Design
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream objectIn = new ObjectInputStream(byteIn);
            return ((Design)objectIn.readObject());
        } catch (Exception e){
            MultiThreadedGeneticAlgorithm.error("Error creating a deep copy of the design held by " + Thread.currentThread().getName());
            return null;
        }
    }

    /**
     * Member class is a nested class which extends Thread. It provides the functionality of the genetic algorithm.
     */
    private class Member extends Thread{
        Design memberDesign;
        Design crossoverMember;
        int ID;

        public Member(int i){
            this.ID = i;
        }

        public void run(){
            // Check there is a design available for the thread to manage
            // If not print an error message and exit the system
            if(data.getDesignsArraySize() == 0){
                MultiThreadedGeneticAlgorithm.error("There are no designs for thread " + ID + " to manage.");
            }
            // Set a design for this thread to manage and remove it from the data file
            // so that no other thread can manage this design
            memberDesign = MultiThreadedGeneticAlgorithm.data.getAndRemoveDesign();

            // While the number of function evaluations has not been met
            while(MultiThreadedGeneticAlgorithm.data.terminationMet() == false){
                // Evaluate the design
                memberDesign.evaluate();

                // If number of function evaulations has been met, break the loop
                if(data.incrementCounter() == true){
                    break;
                }

                // Keep trying to access the eliteDesigns array to see if design should be added
                // Break loop when successful in checking and adding (if necessary)
                while(true){
                    try{
                        //Check if the lock is held on elite designs array
                        if(data.checkEliteSetLock() == false){
                            if(data.addEliteDesign(memberDesign) == true){
                                // If deisgn is added to the elite designs array (if necessary), break loop
                                break;
                            }
                        }
                        this.sleep(10);
                    } catch (InterruptedException e){
                        MultiThreadedGeneticAlgorithm.error("Error whilst waiting for designs to be allocated to threads.");
                    } catch (ConcurrentModificationException e){
                        MultiThreadedGeneticAlgorithm.error("Concurrent error modifying thread.");
                    }
                }

                // Keep trying to access the eliteDesign array or it's most recent copy to select 
                // crossover parent, break loop when a design has been returned
                while(true){
                    try{
                        crossoverMember = data.getRandomEliteDesign();
                        if(crossoverMember != null){
                            // Check a crossover design has been successfully drawn, if so
                            // break loop
                            break;
                        }
                        this.sleep(10);
                    } catch (InterruptedException e){
                        MultiThreadedGeneticAlgorithm.error("Error whilst waiting for designs to be allocated to threads.");
                    } catch (ConcurrentModificationException e){
                        MultiThreadedGeneticAlgorithm.error("Concurrent error modifying thread.");
                    }
                }

                // Create a deep copy of the design managed by this thread to be crossed over with, else
                // if the design managed by this thread is stored in the elite designs array 
                // it will be overwritten with the evolved design
                memberDesign = MultiThreadedGeneticAlgorithm.designDeepCopy(memberDesign);

                // If number of function evaluations has been met, break the loop
                if(data.checkAndSetTermination() == true){
                    break;
                }

                // Evolve design
                memberDesign.evolve(crossoverMember, crossoverRate, mutationRate);
            }
        }
    }

    /**
     * Sorter class is a nested class which extends Thread. The sorter thread is used to sort the elite designs and save the output
     * in both .txt and serialized form once the number of evaluations has been met.
     */
    private class Sorter extends Thread{
        ArrayList<Design> results;

        public void run(){
            // Wait for all functionEvaluations to be completed
            while(data.terminationMet() != true){
                try{
                    this.sleep(10);
                } catch(InterruptedException e){
                    MultiThreadedGeneticAlgorithm.error("Error whilst waiting for design solutions to be updated.");
                }
            }

            // Set elite design array as local variable
            results = data.getEliteDesignsArray();
            // Sort the design
            Collections.sort(results);
            // Reverse the order, so stored best to worst
            Collections.reverse(results);

            try{
                // Write to results.txt file
                BufferedWriter writer = new BufferedWriter(new FileWriter("results.txt"));
                for (int i = 0; i < results.size(); i++){
                    // Write each value on a new line
                    writer.write(results.get(i).getValue().toString());
                    writer.newLine();
                    writer.flush();
                }
                writer.close();

                // Write array to designs.ser serialized file
                FileOutputStream output = new FileOutputStream("designs.ser");
                ObjectOutputStream serializedOutput = new ObjectOutputStream(output);
                serializedOutput.writeObject(results);
                serializedOutput.close();
                output.close();
            } catch (IOException e) {
                // If an IOException is caught, print error message to screen and exit the system
                MultiThreadedGeneticAlgorithm.error("Error writing to results.txt or writing serialized file.");
            } 

            // When files have been written, exit the system
            System.exit(1);
        }
    }
}
