package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import java.util.List;

/**
 *
 */
public class QuerySpecification {
    private final List<String> pastSuccessfulEvents;
    private final List<String> futureEvents;
    private final List<String> oldEvents;
    private final List<String> types;
    /**
     * false = get information from Summa indexes only, true = ask DOMS for all details.
     */
    private boolean details;

    public QuerySpecification(List<String> pastSuccessfulEvents,
                              List<String> futureEvents,
                              List<String> oldEvents,
                              List<String> types,
                              boolean details
                              ) {
        this.pastSuccessfulEvents = pastSuccessfulEvents;
        this.futureEvents = futureEvents;
        this.oldEvents = oldEvents;
        this.types = types;
        this.details = details;
    }

    public List<String> getPastSuccessfulEvents() {
        return pastSuccessfulEvents;
    }

    public List<String> getFutureEvents() {
        return futureEvents;
    }

    public List<String> getOldEvents() {
        return oldEvents;
    }

    public List<String> getTypes() {
        return types;
    }

    public boolean getDetails() {
        return details;
    }
}
