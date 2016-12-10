package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

import java.util.Optional;

/**
 * ToolResult is a helper class for returning a tuple of (result, message[, throwable]) for individual
 * processing steps in a stream, in order for the final collector to decide the overall outcome.  Inspired
 * by the ResultCollector in the newspaper project.
 */
public class ToolResult {
    private final Boolean result;
    private final String humanlyReadableMessage;
    private final Optional<Throwable> throwable;

    private ToolResult(Boolean result, String humanlyReadableMessage, Optional<Throwable> throwable) {
        this.result = result;
        this.humanlyReadableMessage = humanlyReadableMessage;
        this.throwable = throwable;
    }

    public static ToolResult ok(String message) {
        return new ToolResult(Boolean.TRUE, message, Optional.empty());
    }

    public static ToolResult fail(String message, Throwable throwable) {
        return new ToolResult(Boolean.FALSE, message, Optional.of(throwable));
    }

    public static ToolResult fail(String message) {
        return new ToolResult(Boolean.FALSE, message, Optional.empty());
    }

    public String getHumanlyReadableMessage() {
        return humanlyReadableMessage;
    }

    public Optional<Throwable> getThrowable() {
        return throwable;
    }

    public Boolean getResult() {
        return result;
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
