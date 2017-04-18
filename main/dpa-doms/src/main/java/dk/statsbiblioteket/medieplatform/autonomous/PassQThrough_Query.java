package dk.statsbiblioteket.medieplatform.autonomous;


/**
 * This class is a work around that we may need to ask questions that EventTrigger.Query could not answer
 * and the important calling routine is in a maven artifact outside of this project.
 * Hence we subclass the EventTrigger.Query class to do what we need and check for instances of this
 * class in SBOIEventIndex_DigitalPligtafleveringAviser.toQueryString.
 */

public class PassQThrough_Query<T extends Item> extends EventTrigger.Query<T>{
    final String q;

    public PassQThrough_Query(String q) {
        this.q = q;
    }

    public String getQ() {
        return q;
    }
}
