package dk.statsbiblioteket.medieplatform.autonomous.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.Delivery;
import dk.statsbiblioteket.medieplatform.autonomous.DeliveryDomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.DeliveryDomsEventStorageFactory;
import dk.statsbiblioteket.medieplatform.autonomous.Event;

import org.slf4j.Logger;

import java.util.Date;

/**
 * Called from shell script with arguments to create a batch object in DOMS with proper Premis event added.
 *
 * @author jrg
 */
public class CreateBatch {
    private static final String STOPPED_STATE = "Manually_stopped";
    public static Logger log = org.slf4j.LoggerFactory.getLogger(CreateBatch.class);

    /**
     * Receives the following arguments to create a batch object in DOMS:
     * Batch ID, roundtrip number, Premis agent name, URL to DOMS/Fedora, DOMS username, DOMS password,
     * URL to PID generator.
     *
     * @param args The command line arguments received from calling shell script. Explained above.
     */
    public static void main(String[] args) {
        String batchId;
        String roundTrip;
        String premisAgent;
        String domsUrl;
        String domsUser;
        String domsPass;
        String urlToPidGen;
        //Starting to refactor Batch into something that is specifically for "digital pligtaflevering"
        DeliveryDomsEventStorageFactory domsEventStorageFactory = new DeliveryDomsEventStorageFactory();
        DeliveryDomsEventStorage domsEventClient;
        Date now = new Date();

        if (args.length != 8) {
            System.out.println("Not the right amount of arguments");
            System.out.println("Receives the following arguments (in this order) to create a batch object in DOMS:");
            System.out.println(
                    "Batch ID, roundtrip number, Premis agent name, URL to DOMS/Fedora, DOMS username, DOMS password,");
            System.out.println("URL to PID generator.");
            System.exit(1);
        }
        batchId = args[0];
        roundTrip = args[1];
        premisAgent = args[2];
        domsUrl = args[3];
        domsUser = args[4];
        domsPass = args[5];
        urlToPidGen = args[6];
        log.info("Entered main for batch dl_{}_rt{}",batchId,roundTrip);
        domsEventStorageFactory.setFedoraLocation(domsUrl);
        domsEventStorageFactory.setUsername(domsUser);
        domsEventStorageFactory.setPassword(domsPass);
        domsEventStorageFactory.setPidGeneratorLocation(urlToPidGen);
        try {
            domsEventClient = domsEventStorageFactory.createDeliveryDomsEventStorage();
            final int roundTripNumber = Integer.parseInt(roundTrip);
            doWork(new Delivery(batchId, roundTripNumber), premisAgent, domsEventClient, now);
        } catch (Exception e) {
            System.err.println("Failed adding event to batch, due to: " + e.getMessage());
            log.error("Caught exception: ", e);
            System.exit(1);
        }
    }

    /**
     * This will add the state Data_Received for a given batch and roundtrip.
     * This will fail if a later roundtrip exists, and also it will stop earlier roundtrips from processing,
     * should they exist.
     *
     * @param batch The batch to register
     * @param premisAgent The string used as premis agent id
     * @param domsEventClient The doms event client used for registering events.
     * @param now The timestamp to use.
     * @throws CommunicationException On trouble registering event.
     */
    public static void doWork(Delivery batch, String premisAgent, DeliveryDomsEventStorage domsEventClient, Date now) throws CommunicationException {
        boolean alreadyApproved = false;
        boolean newerRoundTripAlreadyReceived = false;


        String message = "";

        /*List<Batch> roundtrips = domsEventClient.getAllRoundTrips(batch.getBatchID());
        if(roundtrips == null) {
            roundtrips = Collections.EMPTY_LIST;
        }
        for (Batch roundtrip : roundtrips) {
            if (roundtrip.getRoundTripNumber() > batch.getRoundTripNumber()) {
                message  +=  "Roundtrip ("+roundtrip.getRoundTripNumber()+") is newer than this roundtrip ("+batch.getRoundTripNumber()+"), so this roundtrip will not be triggered here\n";
                log.warn("Not adding new batch '{}' because a newer roundtrip {} exists", batch.getFullID(), roundtrip.getRoundTripNumber());
                newerRoundTripAlreadyReceived = true;

            }
            if (isApproved(roundtrip)) {
                message  +=  "Roundtrip ("+roundtrip.getRoundTripNumber()+") is already approved, so this roundtrip ("+batch.getRoundTripNumber()+") should not be triggered here\n";
                log.warn("Stopping batch '{}' because another roundtrip {} is already approved", batch.getFullID(), roundtrip.getRoundTripNumber());
                alreadyApproved = true;

            }
        }*/


        domsEventClient.appendEventToItem(batch, premisAgent, now, message, "Data_Received",
                                       !newerRoundTripAlreadyReceived);

        //Inlining to replace above
        /*String itemID = batch.getDomsID();
        if (itemID == null) {
            itemID = "dl_" + batch.getBatchID() + "_rt" + batch.getRoundTripNumber();
            domsEventClient.createBatchRoundTrip(itemID);
            batch.setDomsID(itemID);
        }
        domsEventClient.appendEventToItem(batch, premisAgent, now, message, "Data_Received", !newerRoundTripAlreadyReceived);
*/







        /*if (alreadyApproved){
            domsEventClient.appendEventToItem(batch, premisAgent, now,
                                           "Another Roundtrip is already approved, so this batch should be stopped",
                                           STOPPED_STATE, true);
        } else if (!newerRoundTripAlreadyReceived){
            for (Batch roundtrip : roundtrips) {
                if (!roundtrip.getRoundTripNumber().equals(batch.getRoundTripNumber())) {
                    domsEventClient.appendEventToItem(roundtrip, premisAgent, now,
                                                   "Newer roundtrip (" + batch.getRoundTripNumber()
                                                           + ") has been received, so this batch should be stopped",
                                                   STOPPED_STATE, true);
                    log.warn("Stopping processing of batch '{}' because a newer roundtrip '{}' was received", roundtrip.getFullID(), batch.getFullID());
                }
            }
        }*/
    }






    private static boolean isApproved(Batch olderRoundtrip) {
        for (Event event : olderRoundtrip.getEventList()) {
            if (event.getEventID().equals("Roundtrip_Approved") && event.isSuccess()) {
                return true;
            }
        }
        return false;
    }
}
