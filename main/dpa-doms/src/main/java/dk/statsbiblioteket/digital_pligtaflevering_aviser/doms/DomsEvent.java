package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event;

import java.util.Date;

/**
 *
 */
public class DomsEvent implements Event {
    protected final String linkingAgentIdentifierValue;
    protected final Date timestamp;
    protected final String eventOutcomeDetailNote;
    protected final String eventType;
    protected boolean outcome;

    public DomsEvent(String linkingAgentIdentifierValue, Date timestamp, String eventOutcomeDetailNote, String eventType, boolean outcome) {
        this.linkingAgentIdentifierValue = linkingAgentIdentifierValue;
        this.timestamp = timestamp;
        this.eventOutcomeDetailNote = eventOutcomeDetailNote;
        this.eventType = eventType;
        this.outcome = outcome;
    }

    public String getLinkingAgentIdentifierValue() {
        return linkingAgentIdentifierValue;
    }

    public Date getTimestamp() {
        return timestamp;
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
