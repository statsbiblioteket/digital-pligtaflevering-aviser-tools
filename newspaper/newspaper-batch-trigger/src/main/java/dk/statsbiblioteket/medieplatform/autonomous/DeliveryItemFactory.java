package dk.statsbiblioteket.medieplatform.autonomous;

/**
 * Factory implementation for creation of Delivery from a DeliveryId
 *
 */
public class DeliveryItemFactory implements ItemFactory<Delivery> {

    /**
     * Create a Delivery, which is a subtype of Item
     *
     * @param id the Delivery round trip id, of the form dl_xxxxxx_rtx
     *
     * @return a new Delivery object, without a doms pid
     */
    @Override
    public Delivery create(String id) {
        Delivery.DeliveryRoundtripID splits = new Delivery.DeliveryRoundtripID(id);
        Delivery result = new Delivery(splits.getDeliveryID(), splits.getRoundTripNumber(), Delivery.DeliveryType.STDDELIVERY);
        return result;
    }
}

