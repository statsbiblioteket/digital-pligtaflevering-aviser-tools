package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

/**
 * ToolResult is a helper class for returning a tuple of (result, message[, throwable]) for individual
 * processing steps in a stream, in order for the final collector to decide the overall outcome.  Inspired
 * by the ResultCollector in the newspaper project.
 *
 * FIXME:  AVIS-64 mentioned that the use of DomsItem instead of DomsId might be a memory bottleneck.
 */
public class ToolResult {
    final private DomsItem item;
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

    public ToolResult(DomsItem item, Boolean result, String humanlyReadableMessage) {
        this.item = item;
        this.result = result;
        this.humanlyReadableMessage = humanlyReadableMessage;
    }

    /**
     * ok(message) indicates that this operation went ok, and the message (most likely a summary)
     * may not necessarily be shown to a human except
     * for debugging purposes.
     *
     * @param message informational message for a human
     * @return an ok ToolResult
     */
    public static ToolResult ok(DomsItem item, String message) {
        return new ToolResult(item, Boolean.TRUE, message);
    }


    /**
     * fail(message) is used in the case of normal program flow causing the processing to fail.  The
     * humanly readable message will most likely be seen by a human, and should contain enough information
     * for the human to triage the problem.
     *
     * @param message information message for a human
     * @return a fail ToolResult
     */

    public static ToolResult fail(DomsItem item, String message) {
        return new ToolResult(item, Boolean.FALSE, message);
    }

    public DomsItem getItem() {
        return item;
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
     * Get the result.  TRUE=ok, FALSE=failure
     *
     * @return result
     */
    public Boolean getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "ToolResult{" +
                "item=" + item +
                ", result=" + result +
                ", humanlyReadableMessage='" + humanlyReadableMessage + '\'' +
                '}';
    }
}
