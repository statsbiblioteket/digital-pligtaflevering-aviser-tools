package dk.statsbiblioteket.newspaper.bitrepository.ingester;

/**
 * Exception class to indicate that file a doms object for the file could not be found. 
 */
public class DomsObjectNotFoundException extends Exception {

    public DomsObjectNotFoundException(String message) {
        super(message);
    }
    
    public DomsObjectNotFoundException(String message, Throwable t) {
        super(message, t);
    }
}
