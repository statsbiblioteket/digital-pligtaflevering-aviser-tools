package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event;

/**
 *
 */
public class DomsEvent implements Event {
    private final String agent;
    private final String details;
    private final String eventType;
    private boolean outcome;

    public DomsEvent(String agent, String details, String eventType, boolean outcome) {
        this.agent = agent;
        this.details = details;
        this.eventType = eventType;
        this.outcome = outcome;
    }

    public String getAgent() {
        return agent;
    }

    public String getDetails() {
        return details;
    }

    public String getEventType() {
        return eventType;
    }

    public boolean getOutcome() {
        return outcome;
    }
}
