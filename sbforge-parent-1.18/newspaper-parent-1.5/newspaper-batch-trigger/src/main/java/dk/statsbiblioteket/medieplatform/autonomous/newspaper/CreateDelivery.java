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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dk.statsbiblioteket.medieplatform.autonomous.newspaper.KibanaLoggingStrings.CREATE_DELIVERY_FINISH_LOGTEXT;
import static dk.statsbiblioteket.medieplatform.autonomous.newspaper.KibanaLoggingStrings.CREATE_DELIVERY_START_LOGTEXT;

/**
 * Called from shell script with arguments to create a batch object in DOMS with proper Premis event added.
 */
public class CreateDelivery {
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
     * @param current        The batch to register
     * @param premisAgent     The string used as premis agent id
     * @param domsEventClient The doms event client used for registering events.
     * @throws CommunicationException On trouble registering event.
     */
    public static void doWork(Delivery current, String premisAgent, DeliveryDomsEventStorage domsEventClient) throws CommunicationException {
        //Fedora can not handle ore than one event on the same object at the same time, for this reason Date is not delivered as a parameter
        //It would not fail with the same parameter anyway since there is only written one event

        boolean acceptCurrent = true;

        String message = "";

        List<Delivery> roundtrips = domsEventClient.getAllRoundTrips(current.getDeliveryID());
        if (roundtrips == null) {
            roundtrips = Collections.emptyList();
        }
        
        //Current is not created until first event is added, so it might not be in the roundtrip list.
        //This is ok, as we add the first event to it later on.
        
        
        for (Delivery other : roundtrips) {
            //Make sure that roundtrips which does not have the newest roundtrip-id goes into failed state

            if (other.getFullID().equals(current.getFullID())) {
                continue; //skip the current roundtrip, of course
            }
    
            if (other.getRoundTripNumber() > current.getRoundTripNumber()) {
                //We already have higher-number roundtrip than the one we just received
                
                acceptCurrent = false;
        
                domsEventClient.appendEventToItem(current,
                                                  premisAgent,
                                                  new Date(),
                                                  "Newer roundtrip (" + other.getFullID()
                                                  + ") has already been received, so this roundtrip should be stopped",
                                                  dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event.STOPPED_STATE,
                                                  true);
                
                log.warn("Stopping processing of new delivery '{}' because a higher roundtrip '{}' was already in the system",
                         other.getFullID(),
                         current.getFullID());
        
            }
            
            if (current.getRoundTripNumber() > other.getRoundTripNumber()){
                //The current roundtrip is newer than a previous roundtrip
                
                //Check that the other roundtrip have not already been approved
                boolean otherIsApproved = false;
                for (Event event : other.getEventList()) {
                    if (dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event.APPROVED_STATE.equals(event.getEventID()) && event.isSuccess()){
                        //Other older roundtrip was already approved
                        //We assume that the first succesfull approve event is final. I do not believe people would override it
                        otherIsApproved = true;
                        break;
                    }
                }
                if (otherIsApproved){
                    //The other roundtrip have already been approved, so the current should NOT proceed
                    acceptCurrent = false;
                    domsEventClient.appendEventToItem(current,
                                                      premisAgent,
                                                      new Date(),
                                                      "Older roundtrip (" + other.getFullID()
                                                      + ") has already been approved, so this roundtrip should be stopped",
                                                      dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event.STOPPED_STATE,
                                                      true);
    
                } else {
                    //We already have another lower roundtrip, so stop that. The current roundtrip should take over now
                    domsEventClient.appendEventToItem(other,
                                                      premisAgent,
                                                      new Date(),
                                                      "Newer roundtrip (" + current.getFullID()
                                                      + ") has just been received, so this roundtrip should be stopped",
                                                      dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event.STOPPED_STATE,
                                                      true);
                    log.warn("Stopping processing of delivery '{}' because a newer roundtrip '{}' was received",
                             other.getFullID(),
                             current.getFullID());
                }
            }
            
        }
    
        if (Delivery.DeliveryType.STDDELIVERY.equals(current.getDeliveryType())) {
            domsEventClient.appendEventToItem(current, premisAgent, new Date(), message,
                                              dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event.DATA_RECEIVED, true);
        } else if (Delivery.DeliveryType.MUTATION.equals(current.getDeliveryType())) {
            domsEventClient.appendEventToItem(current, premisAgent, new Date(), message,
                                              dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event.MUTATION_RECEIVED, true);
        }
    
    
    
    }
}
