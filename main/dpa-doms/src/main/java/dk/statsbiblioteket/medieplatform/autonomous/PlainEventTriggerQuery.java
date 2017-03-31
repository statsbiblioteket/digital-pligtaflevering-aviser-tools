package dk.statsbiblioteket.medieplatform.autonomous;


/**
 * This class is a work around that the record base needed in the summa query string is currently hardcoded in
 * SBOIEventIndex in a dependent artifact.
 */

public class PlainEventTriggerQuery<T extends Item> extends EventTrigger.Query<T>{
    final String q;

    public PlainEventTriggerQuery(String q) {
        this.q = q;
    }

    public String getQ() {
        return q;
    }
}
