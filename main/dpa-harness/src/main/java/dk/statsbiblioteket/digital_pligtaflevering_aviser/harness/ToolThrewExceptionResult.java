package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Id;

/**
 *
 */
public class ToolThrewExceptionResult implements Id {
    private final Id id;
    private final Exception exception;

    public ToolThrewExceptionResult(Id id, Exception exception) {
        this.id = id;
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

    public Id getId() {
        return id;
    }


    @Override
    public String id() {
        return id.id();
    }
}
