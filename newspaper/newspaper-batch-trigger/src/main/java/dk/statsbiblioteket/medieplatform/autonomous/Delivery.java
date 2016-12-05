package dk.statsbiblioteket.medieplatform.autonomous;

/**
 * Quick extraction of logic from Batch
 * TODO: Needs to decide what to do about all this
 */
public class Delivery extends Item {

    /**
     * The batch id as a long, ie. without the B in the start of the string
     */
    private String batchID;

    /**
     * The round trip number
     */
    private Integer roundTripNumber = 1;

    /**
     * Constructor
     */
    public Delivery() {
    }

    /**
     * Constructor
     */
    public Delivery(String batchID) {
        setBatchID(batchID);
    }

    /**
     * Constructor
     */
    public Delivery(String batchID, Integer roundTripNumber) {
        if (roundTripNumber == null) {
            roundTripNumber = 0;
        }
        setBatchID(batchID);
        setRoundTripNumber(roundTripNumber);
    }

    /**
     * The round trip number. This will never be less than 1. It counts the number of times a batch
     * have been redelivered
     */
    public Integer getRoundTripNumber() {
        return roundTripNumber;
    }

    /**
     * Set the round trip number
     */
    public void setRoundTripNumber(Integer roundTripNumber) {
        this.roundTripNumber = roundTripNumber;
    }

    /**
     * Get the Batch id.
     *
     * @return as above
     */
    public String getBatchID() {
        return batchID;
    }

    /**
     * Set the batch id
     *
     * @param batchID to set
     */
    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    /**
     * Get the full ID in the form B<batchID>-RT<roundTripNumber>
     *
     * @return the full ID
     */
    @Override
    public String getFullID() {
        return formatFullID(batchID, roundTripNumber);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Batch: " + getFullID());
        if (getEventList() != null && !getEventList().isEmpty()) {
            sb.append(", eventList=").append(getEventList());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Delivery delivery = (Delivery) o;

        if (batchID != null ? !batchID.equals(delivery.batchID) : delivery.batchID != null) return false;
        return roundTripNumber != null ? roundTripNumber.equals(delivery.roundTripNumber) : delivery.roundTripNumber == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (batchID != null ? batchID.hashCode() : 0);
        result = 31 * result + (roundTripNumber != null ? roundTripNumber.hashCode() : 0);
        return result;
    }

    /**
     * Format the batchid and roundtripnumber as a proper batch id
     *
     * @param batchID         the batch id without the leading B
     * @param roundTripNumber the roundtrip number
     * @return a string of the format B{batchID}-RT{roundTripNumber}
     */
    public static String formatFullID(String batchID, Integer roundTripNumber) {
        if (roundTripNumber == null) {
            roundTripNumber = 0;
        }
        return batchID + "_rt" + roundTripNumber;
    }

    public static class BatchRoundtripID {
        private String batchID;
        private int roundTripNumber;

        public BatchRoundtripID(String fullID) {
            String[] splits = fullID.split("_rt");
            if (splits.length == 2) {
                String batchIDsplit = splits[0];
                if (batchIDsplit.startsWith("path:")) {
                    batchIDsplit = batchIDsplit.replace("path:", "");
                }
//                if (batchIDsplit.startsWith("dl_")){
//                    batchIDsplit = batchIDsplit.replace("dl_","");
//                }
//                try {
//                    Long.parseLong(batchIDsplit);
//                } catch (NumberFormatException e) {
//                    throw new IllegalArgumentException("This is not a valid round trip id '" + fullID + "'",e);
//                }
//
                batchID = batchIDsplit;
                String roundTripSplit = splits[1];
                try {
                    roundTripNumber = Integer.parseInt(roundTripSplit);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("This is not a valid round trip id '" + fullID + "'", e);
                }

            } else {
                throw new IllegalArgumentException("This is not a valid round trip id '" + fullID + "'");
            }
        }

        public BatchRoundtripID(String batchID, int roundTripNumber) {
            this.batchID = batchID;
            this.roundTripNumber = roundTripNumber;
        }

        public String getBatchID() {
            return batchID;
        }

        public int getRoundTripNumber() {
            return roundTripNumber;
        }

        public String batchDCIdentifier() {
            return "path:" + batchID;
        }

        public String roundTripDCIdentifier() {
            return "path:" + batchID + "_rt" + roundTripNumber;
        }
    }
}
