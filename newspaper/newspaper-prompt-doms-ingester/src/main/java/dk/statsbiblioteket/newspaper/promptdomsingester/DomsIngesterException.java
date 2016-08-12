package dk.statsbiblioteket.newspaper.promptdomsingester;

/**
 * Exception thrown when the structure to be ingested in DOMS cannot be interpreted according to the expected rules.
 */
public class DomsIngesterException extends Exception {
    public DomsIngesterException(String message) {
        super(message);
    }

    public DomsIngesterException(Throwable cause) {
        super(cause);
    }
}
