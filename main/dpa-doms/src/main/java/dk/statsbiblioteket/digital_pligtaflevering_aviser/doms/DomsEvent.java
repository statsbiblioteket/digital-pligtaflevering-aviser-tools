package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event;

/**
 *
 */
public class DomsEvent implements Event {
    private final String linkingAgentIdentifierValue;
    private final String eventOutcomeDetailNote;
    private final String eventType;
    private boolean outcome;

    public DomsEvent(String linkingAgentIdentifierValue, String eventOutcomeDetailNote, String eventType, boolean outcome) {
        this.linkingAgentIdentifierValue = linkingAgentIdentifierValue;
        this.eventOutcomeDetailNote = eventOutcomeDetailNote;
        this.eventType = eventType;
        this.outcome = outcome;
    }

    public String getLinkingAgentIdentifierValue() {
        return linkingAgentIdentifierValue;
    }

    public String getEventOutcomeDetailNote() {
        return eventOutcomeDetailNote;
    }

    public String getEventType() {
        return eventType;
    }

    public boolean getOutcome() {
        return outcome;
    }
}
