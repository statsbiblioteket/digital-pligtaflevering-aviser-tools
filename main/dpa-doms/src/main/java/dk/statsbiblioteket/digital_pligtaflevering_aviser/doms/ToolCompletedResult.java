package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Id;

public class ToolCompletedResult implements Id {
    protected final Id id;
    protected final boolean success;
    protected final String payload;

    public ToolCompletedResult(Id id, boolean success, String payload) {
        this.id = id;
        this.success = success;
        this.payload = payload;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getPayload() {
        return payload;
    }

    public Id getId() {
        return id;
    }

    @Override
    public String id() {
        return id.id();
    }


}
