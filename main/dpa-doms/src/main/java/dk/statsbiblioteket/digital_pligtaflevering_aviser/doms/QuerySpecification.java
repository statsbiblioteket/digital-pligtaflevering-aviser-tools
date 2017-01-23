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
     * false = get information from Summa indexes only, true = populate events in answer.
     */
    private boolean details;

    /**
     * Hold a complete SBOI query (maps to <code>EventTrigger.Query&lt;Item></code>) querying a repository for
     * items fulfilling a given set of criterias.  Each event is a simple string and the same as
     * is used when adding an event to an item.
     * @param pastSuccessfulEvents list of events that must have happened with a successful result
     * @param futureEvents list of events that must never have happened
     * @param oldEvents list of events that must either FIXME (what exactly) or never have happened.
     * @param types FIXME
     * @param details false for a simple response, true for all details filled out.
     */
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

    @Override
    public String toString() {
        return "QuerySpecification{" +
                "pastSuccessfulEvents=" + pastSuccessfulEvents +
                ", futureEvents=" + futureEvents +
                ", oldEvents=" + oldEvents +
                ", types=" + types +
                ", details=" + details +
                '}';
    }
}
