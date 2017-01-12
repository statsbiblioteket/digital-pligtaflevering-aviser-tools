package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.ObjectIsWrongTypeException;
import dk.statsbiblioteket.doms.central.connectors.fedora.utils.FedoraUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Quick extraction of logic from BatchDomsEventStorage
 */
public class DeliveryDomsEventStorage extends DomsEventStorage<Delivery> {

    private static Logger log = LoggerFactory.getLogger(DeliveryDomsEventStorage.class);

    private final String deliveryTemplate;
    private final String roundTripTemplate;
    private final String hasPart_relation;


    private final String createDeliveryRoundTripComment = "Creating delivery round trip";

    public DeliveryDomsEventStorage(EnhancedFedora fedora, String type, String deliveryTemplate, String roundTripTemplate,
                                    String hasPart_relation, String eventsDatastream, ItemFactory<Delivery> itemFactory) throws JAXBException {
        super(fedora, type, eventsDatastream,
                itemFactory);
        this.deliveryTemplate = deliveryTemplate;
        this.roundTripTemplate = roundTripTemplate;
        this.hasPart_relation = hasPart_relation;
    }

    @Override
    public Date appendEventToItem(Delivery item, String agent, Date timestamp, String details, String eventType,
                                  boolean outcome) throws CommunicationException {
        String itemID = item.getDomsID();
        if (itemID == null) {
            itemID = createDeliveryRoundTrip(item.getFullID());
            item.setDomsID(itemID);
        }
        try {
            return super.appendEventToItem(item, agent, timestamp, details, eventType, outcome);
        } catch (NotFoundException e) {
            throw new CommunicationException(e);
        }
    }

    /**
     * Create a batch and round trip object, without adding any events
     *
     * @param fullItemID the full item id
     *
     * @return the pid of the doms object corresponding to the round trip
     * @throws dk.statsbiblioteket.medieplatform.autonomous.CommunicationException if communication with doms failed
     */
    public String createDeliveryRoundTrip(String fullItemID) throws CommunicationException {
        try {
            try {
                //find the roundTrip Object
                return getPidFromDCIdentifier(fullItemID);
            } catch (NotFoundException e) {
                //no roundTripObject, so sad
                //but alas, we can continue
            }

            //find the batch object
            String deliveryObject;
            Delivery.DeliveryRoundtripID fullIDSplits = new Delivery.DeliveryRoundtripID(fullItemID);
            List<String> founds = fedora.findObjectFromDCIdentifier(fullIDSplits.batchDCIdentifier());
            if (founds.size() > 0) {
                deliveryObject = founds.get(0);
            } else {
                //no batch object either, more sad
                //create it, then
                deliveryObject = fedora.cloneTemplate(
                        deliveryTemplate, Arrays.asList(fullIDSplits.batchDCIdentifier()), createDeliveryRoundTripComment);
            }
            String roundTripObject;

            roundTripObject = fedora.cloneTemplate(roundTripTemplate, Arrays.asList(fullIDSplits.roundTripDCIdentifier()), createDeliveryRoundTripComment);

            //connect batch object to round trip object
            fedora.addRelation(
                    deliveryObject,
                    FedoraUtil.ensureURI(deliveryObject),
                    hasPart_relation,
                    FedoraUtil.ensureURI(roundTripObject),
                    false,
                    createDeliveryRoundTripComment);

            //create the initial EVENTS datastream

            String premisBlob = premisFactory.createInitialPremisBlob(fullItemID).toXML();
            fedora.modifyDatastreamByValue(
                    roundTripObject, eventsDatastream, null,null,premisBlob.getBytes(), null, "text/xml", createDeliveryRoundTripComment,null);


            return roundTripObject;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | PIDGeneratorException |
                BackendInvalidResourceException | ObjectIsWrongTypeException | JAXBException e) {
            throw new CommunicationException(e);
        }
    }


    /**
     * Returns all Batch roundtrip objects for a given batchId, sorted in ascending order.
     * Returns null if the batchId is not known.
     * @param batchId the batchId.
     * @return the sorted list of roundtrip objects.
     */
    public List<Delivery> getAllRoundTrips(String batchId) throws CommunicationException {
        Comparator<Delivery> roundtripComparator = new Comparator<Delivery>() {
            @Override
            public int compare(Delivery o1, Delivery o2) {
                return o1.getRoundTripNumber().compareTo(o2.getRoundTripNumber());
            }
        };
        try {
            List<String> founds = fedora.findObjectFromDCIdentifier(new Delivery.DeliveryRoundtripID(batchId,0).batchDCIdentifier());
            if (founds == null || founds.size() == 0) {
                return null;
            }
            String batchObjectPid = founds.get(0);
            List<FedoraRelation> roundtripRelations = fedora.getNamedRelations(batchObjectPid, hasPart_relation, null);
            List<Delivery> roundtrips = new ArrayList<>();
            for (FedoraRelation roundtripRelation: roundtripRelations) {
                try {
                    final Delivery itemFromDomsID = getItemFromDomsID(FedoraUtil.ensurePID(roundtripRelation.getObject()));
                    if (itemFromDomsID != null) {
                        roundtrips.add(itemFromDomsID);
                    }
                } catch (NotFoundException ignored) {

                }
            }
            Collections.sort(roundtrips, roundtripComparator);
            return roundtrips;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | BackendInvalidResourceException e) {
            throw new CommunicationException(e);
        }
    }
}
