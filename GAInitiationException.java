package ga;

/**
 * ClassNotProblemTypeException, holds details when a type name is passed for use
 * but it is not a Problem subtype.
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class GAInitiationException extends Exception
{
   /**
    * Constructor accepts a message to be contained in this instance
    * 
    * @param message the message to contain
    */ 
   public GAInitiationException(String message) {
       super(message);
   }
}
