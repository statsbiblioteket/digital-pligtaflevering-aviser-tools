package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import java.util.List;

/**
 *
 */
public class EventQuerySpecification implements QuerySpecification {
    private final List<String> pastSuccessfulEvents;
    private final List<String> futureEvents;
    private final List<String> oldEvents;
    private final List<String> types;
    /**
     * false = get information from Summa indexes only, true = populate events in answer.
     */
    private boolean details;

    public EventQuerySpecification(List<String> pastSuccessfulEvents,
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

    @Override
    public String toString() {
        return "EventQuerySpecification{" +
                "pastSuccessfulEvents=" + pastSuccessfulEvents +
                ", futureEvents=" + futureEvents +
                ", oldEvents=" + oldEvents +
                ", types=" + types +
                ", details=" + details +
                '}';
    }
}
