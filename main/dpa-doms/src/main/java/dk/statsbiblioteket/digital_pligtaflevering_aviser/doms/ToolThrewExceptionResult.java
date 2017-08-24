package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Id;

/**
 *
 */
public class ToolThrewExceptionResult implements Id {
    protected final DomsItem item;
    protected final Exception exception;

    public ToolThrewExceptionResult(DomsItem item, Exception exception) {
        this.item = item;
        this.exception = exception;
    }

    public DomsItem getItem() {
        return item;
    }

    public Exception getException() {
        return exception;
    }


    @Override
    public String id() {
        return item.getDomsId().id();
    }
}
