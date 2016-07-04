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

    public QuerySpecification(List<String> pastSuccessfulEvents,
                              List<String> futureEvents,
                              List<String> oldEvents,
                              List<String> types) {
        this.pastSuccessfulEvents = pastSuccessfulEvents;
        this.futureEvents = futureEvents;
        this.oldEvents = oldEvents;
        this.types = types;
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
}
