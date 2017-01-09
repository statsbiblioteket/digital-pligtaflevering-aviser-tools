package dk.statsbiblioteket.medieplatform.autonomous.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.Delivery;
import dk.statsbiblioteket.medieplatform.autonomous.DeliveryDomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.DeliveryDomsEventStorageFactory;
import dk.statsbiblioteket.medieplatform.autonomous.Event;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static dk.statsbiblioteket.medieplatform.autonomous.newspaper.LoggingKeywords.CREATE_DELIVERY_FINISH_LOGTEXT;
import static dk.statsbiblioteket.medieplatform.autonomous.newspaper.LoggingKeywords.CREATE_DELIVERY_START_LOGTEXT;

/**
 * Called from shell script with arguments to create a batch object in DOMS with proper Premis event added.
 *
 * @author jrg
 */
public class CreateDelivery {
    private static final String STOPPED_STATE = "Manually_stopped";
    public static Logger log = org.slf4j.LoggerFactory.getLogger(CreateDelivery.class);

    /**
     * Receives the following arguments to create a batch object in DOMS:
     * Batch ID, roundtrip number, Premis agent name, URL to DOMS/Fedora, DOMS username, DOMS password,
     * URL to PID generator.
     *
     * @param args The command line arguments received from calling shell script. Explained above.
     */
    public static void main(String[] args) {
        String deliveryId;
        String roundTrip;
        String premisAgent;
        String domsUrl;
        String domsUser;
        String domsPass;
        String urlToPidGen;
        //Starting to refactor Delivery into something that is specifically for "digital pligtaflevering"
        DeliveryDomsEventStorageFactory domsEventStorageFactory = new DeliveryDomsEventStorageFactory();
        DeliveryDomsEventStorage domsEventClient;
        Date now = new Date();

        if (args.length != 8) {
            log.error("Not the right amount of arguments");
            log.error("Receives the following arguments (in this order) to create a batch object in DOMS:");
            log.error("Deliveries ID, roundtrip number, Premis agent name, URL to DOMS/Fedora, DOMS username, DOMS password,");
            log.error("URL to PID generator.");
            System.exit(1);
        }
        deliveryId = args[0];
        roundTrip = args[1];
        premisAgent = args[2];
        domsUrl = args[3];
        domsUser = args[4];
        domsPass = args[5];
        urlToPidGen = args[6];
        long startDeliveryTime = System.currentTimeMillis();
        log.info(CREATE_DELIVERY_START_LOGTEXT, deliveryId, roundTrip);
        domsEventStorageFactory.setFedoraLocation(domsUrl);
        domsEventStorageFactory.setUsername(domsUser);
        domsEventStorageFactory.setPassword(domsPass);
        domsEventStorageFactory.setPidGeneratorLocation(urlToPidGen);
        try {
            domsEventClient = domsEventStorageFactory.createDeliveryDomsEventStorage();
            final int roundTripNumber = Integer.parseInt(roundTrip);
            doWork(new Delivery(deliveryId, roundTripNumber), premisAgent, domsEventClient, now);
            long finishedDeliveryTime = System.currentTimeMillis();
            log.info(CREATE_DELIVERY_FINISH_LOGTEXT, deliveryId, roundTrip, finishedDeliveryTime - startDeliveryTime);
        } catch (Exception e) {
            log.error("Failed adding event to batch, due to: " + e.getMessage());
            log.error("Caught exception: ", e);
            System.exit(1);
        }
    }

    /**
     * This will add the state Data_Received for a given batch and roundtrip.
     * This will fail if a later roundtrip exists, and also it will stop earlier roundtrips from processing,
     * should they exist.
     *
     *
     * @param delivery The batch to register
     * @param premisAgent The string used as premis agent id
     * @param domsEventClient The doms event client used for registering events.
     * @param now The timestamp to use.
     * @throws CommunicationException On trouble registering event.
     */
    public static void doWork(Delivery delivery, String premisAgent, DeliveryDomsEventStorage domsEventClient, Date now) throws CommunicationException {
        boolean alreadyApproved = false;
        boolean newerRoundTripAlreadyReceived = false;


        String message = "";

        List<Delivery> roundtrips = domsEventClient.getAllRoundTrips(delivery.getDeliveryID());
        if(roundtrips == null) {
            roundtrips = Collections.emptyList();
        }
        for (Delivery roundtrip : roundtrips) {
            if (roundtrip.getRoundTripNumber() > delivery.getRoundTripNumber()) {
                message  +=  "Roundtrip ("+roundtrip.getRoundTripNumber()+") is newer than this roundtrip ("+delivery.getRoundTripNumber()+"), so this roundtrip will not be triggered here\n";
                log.warn("Not adding new batch '{}' because a newer roundtrip {} exists", delivery.getFullID(), roundtrip.getRoundTripNumber());
                newerRoundTripAlreadyReceived = true;
            }
            if (isApproved(roundtrip)) {
                message  +=  "Roundtrip ("+roundtrip.getRoundTripNumber()+") is already approved, so this roundtrip ("+delivery.getRoundTripNumber()+") should not be triggered here\n";
                log.warn("Stopping batch '{}' because another roundtrip {} is already approved", delivery.getFullID(), roundtrip.getRoundTripNumber());
                alreadyApproved = true;
            }
        }


        domsEventClient.appendEventToItem(delivery, premisAgent, now, message, "Data_Received",
                                       !newerRoundTripAlreadyReceived);

        /*domsEventClient.appendEventToItem(delivery, premisAgent, now, message, "Data_T2",
                !newerRoundTripAlreadyReceived);*/


        if (alreadyApproved){
            domsEventClient.appendEventToItem(delivery, premisAgent, now,
                                           "Another Roundtrip is already approved, so this batch should be stopped",
                                           STOPPED_STATE, true);
        } else {
            domsEventClient.appendEventToItem(delivery, premisAgent, now, message, "Roundtrip_Approved",
                    !newerRoundTripAlreadyReceived);
        }
    }






    private static boolean isApproved(Delivery olderRoundtrip) {
        for (Event event : olderRoundtrip.getEventList()) {
            if (event.getEventID().equals("Roundtrip_Approved") && event.isSuccess()) {
                return true;
            }
        }
        return false;
    }
}
