package dk.statsbiblioteket.medieplatform.autonomous;

/**
 * Delivery forms an object representation of a delivery-folder of newspapers
 */
public class Delivery extends Item {

    /**
     * The batch id as a long, ie. without the "dl_" in the start of the string
     */
    private String deliveryID;

    /**
     * The round trip number
     */
    private Integer roundTripNumber = 1;

    /**
     * DeliveryType, this can be either a std. delivery or a mutation
     */
    private DeliveryType deliveryType;


    /**
     * Constructor for Deliverys, deliverys can be of two types, either a std. delivery or a mutation
     */
    public Delivery(String deliveryID, Integer roundTripNumber, DeliveryType deliveryType) {
        if (roundTripNumber == null) {
            roundTripNumber = 0;
        }
        setDeliveryID(deliveryID);
        setRoundTripNumber(roundTripNumber);
        this.deliveryType = deliveryType;
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
    public String getDeliveryID() {
        return deliveryID;
    }

    /**
     * Get the deliveryType id.
     *
     * @return as above
     */
    public DeliveryType getDeliveryType() {
        return deliveryType;
    }


    /**
     * Set the batch id
     *
     * @param deliveryID to set
     */
    public void setDeliveryID(String deliveryID) {
        this.deliveryID = deliveryID;
    }

    /**
     * Get the full ID in the form B<deliveryID>-RT<roundTripNumber>
     *
     * @return the full ID
     */
    @Override
    public String getFullID() {
        return formatFullID(deliveryID, roundTripNumber);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Delivery: " + getFullID());
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

        if (deliveryID != null ? !deliveryID.equals(delivery.deliveryID) : delivery.deliveryID != null) return false;
        return roundTripNumber != null ? roundTripNumber.equals(delivery.roundTripNumber) : delivery.roundTripNumber == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (deliveryID != null ? deliveryID.hashCode() : 0);
        result = 31 * result + (roundTripNumber != null ? roundTripNumber.hashCode() : 0);
        return result;
    }

    /**
     * Format the batchid and roundtripnumber as a proper batch id
     *
     * @param deliveryID         the delivery id without the leading dl_
     * @param roundTripNumber the roundtrip number
     * @return a string of the format dl_{deliveryID}_rt{roundTripNumber}
     */
    public static String formatFullID(String deliveryID, Integer roundTripNumber) {
        if (roundTripNumber == null) {
            roundTripNumber = 0;
        }
        return deliveryID + "_rt" + roundTripNumber;
    }

    public static class DeliveryRoundtripID {
        private String deliveryID;
        private int roundTripNumber;

        public DeliveryRoundtripID(String fullID) {
            String[] splits = fullID.split("_rt");
            if (splits.length == 2) {
                String batchIDsplit = splits[0];
                if (batchIDsplit.startsWith("path:")) {
                    batchIDsplit = batchIDsplit.replace("path:", "");
                }

                deliveryID = batchIDsplit;
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

        public DeliveryRoundtripID(String deliveryID, int roundTripNumber) {
            this.deliveryID = deliveryID;
            this.roundTripNumber = roundTripNumber;
        }

        public String getDeliveryID() {
            return deliveryID;
        }

        public int getRoundTripNumber() {
            return roundTripNumber;
        }

        public String batchDCIdentifier() {
            return "path:" + deliveryID;
        }

        public String roundTripDCIdentifier() {
            return "path:" + deliveryID + "_rt" + roundTripNumber;
        }
    }

    public enum DeliveryType {
        STDDELIVERY, MUTATION
    }
}
