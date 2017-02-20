package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * ToolResult is a helper class for returning a tuple of (result, message[, throwable]) for individual
 * processing steps in a stream, in order for the final collector to decide the overall outcome.  Inspired
 * by the ResultCollector in the newspaper project.
 */
public class ToolResult {
    /**
     * Holds the result of the operation.  TRUE=successful.  FALSE=failed.
     */
    private final Boolean result;
    /**
     * Holds a message intended to be read by a human.  Do not use this for anything else.
     */
    private final String humanlyReadableMessage;

    /**
     * Optionally holds an exception indicating what went wrong.  This will most likely end up in a log file and/or
     * in a DOMS datastream.
     */

    private final Optional<Throwable> throwable;

    private ToolResult(Boolean result, String humanlyReadableMessage, Optional<Throwable> throwable) {
        this.result = result;
        this.humanlyReadableMessage = humanlyReadableMessage;
        this.throwable = throwable;
    }

    /**
     * ok(message) indicates that this operation went ok, and the message (most likely a summary)
     * may not necessarily be shown to a human except
     * for debugging purposes.
     *
     * @param message informational message for a human
     * @return an ok ToolResult
     */
    public static ToolResult ok(String message) {
        return new ToolResult(Boolean.TRUE, message, Optional.empty());
    }

    /**
     * fail(message, throwable) is used in the case of an exception happening causing the processing to fail.  The
     * humanly readable message will most likely be seen by a human, and should contain enough information (in addition
     * to the exception provided) for the human to triage the problem.
     *
     * @param message   information message for a human
     * @param throwable exception caught causing the failure
     * @return a fail ToolResult
     */
    @Deprecated
    public static ToolResult fail(String message, Throwable throwable) {
        return new ToolResult(Boolean.FALSE, message, Optional.of(throwable));
    }

    /**
     * fail(message) is used in the case of normal program flow causing the processing to fail.  The
     * humanly readable message will most likely be seen by a human, and should contain enough information
     * for the human to triage the problem.
     *
     * @param message information message for a human
     * @return a fail ToolResult
     */

    public static ToolResult fail(String message) {
        return new ToolResult(Boolean.FALSE, message, Optional.empty());
    }

    /**
     * Get the message intended for a human to read
     *
     * @return message
     */
    public String getHumanlyReadableMessage() {
        return humanlyReadableMessage;
    }

    /**
     * Get the Optional which may hold a Throwable for this message (if one was provided)
     *
     * @return optional exception
     */
    @Deprecated
    public Optional<Throwable> getThrowable() {
        return throwable;
    }

    /**
     * Get the result.  TRUE=ok, FALSE=failure
     *
     * @return result
     */
    public Boolean getResult() {
        return result;
    }

    @Deprecated
    public String getHumanlyReadableStackTrace() {
        if (throwable.isPresent() == false) {
            return "";
        }
        // http://stackoverflow.com/a/18546861/53897
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        pw.println("---");
        throwable.get().printStackTrace(pw);
        pw.println("---");
        return sw.getBuffer().toString();
    }


    @Override
    public String toString() {
        return "ToolResult{" +
                "result=" + result +
                ", humanlyReadableMessage='" + humanlyReadableMessage + '\'' +
                ", throwable=" + throwable +
                '}';
    }
}
