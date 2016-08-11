package dk.statsbiblioteket.newspaper.promptdomsingester;

/**
 * This exception indicated that something, such as a lost zookeeper connection, cause the ingester to shut down.
 * The ingester shut down correct. If it did not shut down correctly, you would not see this exception, as the JVM
 * would be dead.
 */
public class IngesterShutdownException extends RuntimeException {
    public IngesterShutdownException() {
    }

    public IngesterShutdownException(String message) {
        super(message);
    }

    public IngesterShutdownException(String message, Throwable cause) {
        super(message, cause);
    }

    public IngesterShutdownException(Throwable cause) {
        super(cause);
    }

    public IngesterShutdownException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
