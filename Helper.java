package ga;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * The Helper class is soley to provide a static method to help with the conversion of a String 
 * passed to your program into a valid Problem instance. It exploits reflection, which we will 
 * see more of toward the end of the ECM2414 lecture series
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class Helper
{
    /**
     * Given the name argument, the method will attempt to initialise a Problem instance which
     * corresponds to the name (and will through an exception if the name does not match an 
     * available class, is not a problem instance, does not provide a no argument constructor
     * as required by the Problem API, etc.)
     * 
     * @param name a String holding the fully qualified name of the Problem class to consruct
     * @return a Problem instance of the type specified in name
     * @throws GAInitiationException if there are any problems generating the requested class
     */
    public static Problem getProblem(String name) throws GAInitiationException {
        /*
         * Use reflection to create instance of a Problem implementor used by the optimiser
         */
        Object object = null;
        try {
            Class<?> c = Class.forName(name);
            Constructor<?> constructor = c.getConstructor(); // get no argument constructor
            object = constructor.newInstance();
        } catch(IndexOutOfBoundsException e) {
            throw new GAInitiationException("Must invoke MultiThreadedGeneticAlgorithm program "
                + "with an argument containing the class name of the ga.Problem implementor to "
                + "be used by the GA");
        } catch(ClassNotFoundException e) {
            throw new GAInitiationException("Class definition not found matching name entered");
        } catch(NoSuchMethodException e) {
            throw new GAInitiationException("All implementors of the ga.Problem interface must "
                + "provide a public no-argument constructor");                  
        } catch(InstantiationException e) {
            throw new GAInitiationException("Problem encounterd generating an instance -- have " 
                + "you checked that it is not an abstract class or an interface you have "
                + "entered the details for?");
         } catch(IllegalAccessException e) {
            throw new GAInitiationException("Problem encounterd generating an instance -- have "
                + "ensured the correct path to the compiled bytecode of the specified class "
                + "file was entered/is accessible");                
        } catch(InvocationTargetException e) {
            throw new GAInitiationException("Problem encounterd generating an instance -- "
                + "Constructor threw an exception. Details follow: ... " + e.getMessage());
        }
        
        if (object instanceof Problem) {
            return (Problem) object;
        }                 
        throw new GAInitiationException("Class name entered must be a subtype of ga.Problem");       
    }
}
