package dk.statsbiblioteket.medieplatform.autonomous;

/**
 * Quick extraction of logic from BatchItemFactory
 * TODO: Needs to decide what to do about all this
 */
public class DeliveryItemFactory implements ItemFactory<Delivery> {

    /**
     * Create a Delivery, which is a subtype of Item
     *
     * @param id the batch round trip id, of the form Bxxxxxx-RTx
     *
     * @return a new Delivery object, without a doms pid
     */
    @Override
    public Delivery create(String id) {
        Delivery.BatchRoundtripID splits = new Delivery.BatchRoundtripID(id);
        Delivery result = new Delivery(splits.getBatchID());
        result.setRoundTripNumber(splits.getRoundTripNumber());
        return result;
    }
}

