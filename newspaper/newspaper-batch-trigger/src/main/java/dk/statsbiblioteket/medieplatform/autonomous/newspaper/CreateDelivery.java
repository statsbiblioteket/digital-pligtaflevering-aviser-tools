package dk.statsbiblioteket.medieplatform.autonomous.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.Delivery;
import dk.statsbiblioteket.medieplatform.autonomous.DeliveryDomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.DeliveryDomsEventStorageFactory;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dk.statsbiblioteket.medieplatform.autonomous.newspaper.KibanaLoggingStrings.CREATE_DELIVERY_FINISH_LOGTEXT;
import static dk.statsbiblioteket.medieplatform.autonomous.newspaper.KibanaLoggingStrings.CREATE_DELIVERY_START_LOGTEXT;

/**
 * Called from shell script with arguments to create a batch object in DOMS with proper Premis event added.
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

            // Match that the deliverys contains the expected string, either "dl_yyyymmdd" or "mt_yyyymmdd_no#"

            //deliveryPatterMatcher
            Pattern deliveryPattern = Pattern.compile("^dl_+([\\d]{8}+)$");
            Matcher matcher = deliveryPattern.matcher(deliveryId);

            //mutationPatterMatcher
            Pattern mutationPattern = Pattern.compile("^mt_+([\\d]{8})+_no([0-9]+)$");

            if (deliveryPattern.matcher(deliveryId).matches()) {
                doWork(new Delivery(deliveryId, roundTripNumber, Delivery.DeliveryType.STDDELIVERY), premisAgent, domsEventClient);
            } else if (mutationPattern.matcher(deliveryId).matches()) {
                doWork(new Delivery(deliveryId, roundTripNumber, Delivery.DeliveryType.MUTATION), premisAgent, domsEventClient);
            } else {
                log.error("The folder with the name " + deliveryId + " is not recognized by this component");
            }

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
     * @param delivery        The batch to register
     * @param premisAgent     The string used as premis agent id
     * @param domsEventClient The doms event client used for registering events.
     * @throws CommunicationException On trouble registering event.
     */
    public static void doWork(Delivery delivery, String premisAgent, DeliveryDomsEventStorage domsEventClient) throws CommunicationException {
        //Fedora can not handle ore than one event on the same object at the same time, for this reason Date is not delivered as a parameter
        //It would not fail with the same parameter anyway since there is only written one event
        boolean newerRoundTripAlreadyReceived = false;

        String message = "";

        List<Delivery> roundtrips = domsEventClient.getAllRoundTrips(delivery.getDeliveryID());
        if (roundtrips == null) {
            roundtrips = Collections.emptyList();
        }
        for (Delivery roundtrip : roundtrips) {
            //Make sure that roundtrips which does not have the newest roundtrip-id goes into failed state
            if (roundtrip.getRoundTripNumber() > delivery.getRoundTripNumber()) {
                message += "Roundtrip (" + roundtrip.getRoundTripNumber() + ") is newer than this roundtrip (" + delivery.getRoundTripNumber() + "), so this roundtrip will not be triggered here\n";
                log.warn("Not adding new delivery '{}' because a newer roundtrip {} exists", delivery.getFullID(), roundtrip.getRoundTripNumber());
                newerRoundTripAlreadyReceived = true;
            }
        }


        if (Delivery.DeliveryType.STDDELIVERY.equals(delivery.getDeliveryType())) {
            domsEventClient.appendEventToItem(delivery, premisAgent, new Date(), message, "Data_Received", !newerRoundTripAlreadyReceived);
        } else if (Delivery.DeliveryType.MUTATION.equals(delivery.getDeliveryType())) {
            domsEventClient.appendEventToItem(delivery, premisAgent, new Date(), message, "Mutation_Received", !newerRoundTripAlreadyReceived);
        }

        if (!newerRoundTripAlreadyReceived) {
            for (Delivery roundtrip : roundtrips) {
                if (!roundtrip.getRoundTripNumber().equals(delivery.getRoundTripNumber())) {
                    domsEventClient.appendEventToItem(roundtrip, premisAgent, new Date(),
                            "Newer roundtrip (" + roundtrip.getRoundTripNumber()
                                    + ") has been received, so this batch should be stopped", STOPPED_STATE, true);
                    log.warn("Stopping processing of batch '{}' because a newer roundtrip '{}' was received", roundtrip.getFullID(), roundtrip.getFullID());
                }
            }
        }
    }
}
