package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Id;

/**
 * ToolResult is a helper class for returning a tuple of (success, message[, throwable]) for individual
 * processing steps in a stream, in order for the final collector to decide the overall outcome.  Inspired
 * by the ResultCollector in the newspaper project.
 *
 * FIXME:  AVIS-64 mentioned that the use of DomsItem instead of DomsId might be a memory bottleneck.
 */
public class ToolResult implements Id {
    private final DomsItem item;
    /**
     * Holds the success of the operation.  TRUE=successful.  FALSE=failed.
     */
    private final boolean success;
    /**
     * Holds a message intended to be read by a human.  Do not use this for anything else.
     */
    private final String humanlyReadableMessage;
    /**
     * Optionally holds an exception indicating what went wrong.  This will most likely end up in a log file and/or
     * in a DOMS datastream.
     */

    public ToolResult(DomsItem item, boolean success, String humanlyReadableMessage) {
        this.item = item;
        this.success = success;
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
     * Get the success.  TRUE=ok, FALSE=failure
     *
     * @return success
     */
    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "ToolResult{" +
                "item=" + item +
                ", success=" + success +
                ", humanlyReadableMessage='" + humanlyReadableMessage + '\'' +
                '}';
    }

    @Override
    public String id() {
        return item.getDomsId().id();
    }
}
