package dk.statsbiblioteket.digital_pligtaflevering_aviser.triggers;

import dk.statsbiblioteket.medieplatform.autonomous.Item;

/**
 *  BARE BONES - JUST ENOUGH TO ALLOW COMPILATION
 */
public class Delivery extends Item {

    private Delivery batchID;
    private String fullID;

    public Delivery getBatchID() {
        return batchID;
    }

    public void setBatchID(Delivery batchID) {
        this.batchID = batchID;
    }

    public Long getRoundTripNumber() {
        throw new UnsupportedOperationException();
    }

    public String getFullID() {
        return fullID;
    }

    public void setFullID(String fullID) {
        this.fullID = fullID;
    }
}
