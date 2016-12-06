package dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.newspaper.bitrepository.ingester.DomsObjectNotFoundException;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.protocol.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.newspaper.bitrepository.ingester.DomsFileUrlRegister;

public class PutFileEventHandler implements EventHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ParallelOperationLimiter operationLimiter;
    private final DomsFileUrlRegister domsRegister;
    private final BlockingQueue<PutJob> failedJobs=null;

    
    /*public PutFileEventHandler(ParallelOperationLimiter putLimiter, BlockingQueue<PutJob> failedJobsQueue,
            DomsFileUrlRegister domsRegister) {
    	this.operationLimiter = putLimiter;
        this.domsRegister = domsRegister;
        this.failedJobs = failedJobsQueue;
    }*/



    public PutFileEventHandler(ParallelOperationLimiter putLimiter, BlockingQueue<PutJob> failedJobsQueue,
                               DomsFileUrlRegister domsRegister) {

        this.operationLimiter = putLimiter;
        this.domsRegister = domsRegister;


    }


    @Override
    public void handleEvent(OperationEvent event) {

        try {


            /*completeEvent.setFileID(fileId);
            completeEvent.setOperationType(OperationType.PUT_FILE);


            domsRegister.registerJp2File();


            List<String> objects;
            Date start = new Date();
            String path = job.getIngestableFile().getPath();
            objects = efedora.findObjectFromDCIdentifier(path);
            Date objFound = new Date();
            log.trace("It took {} ms to find object in doms for path '{}'", objFound.getTime() - start.getTime(), path);
            if (objects.size() != 1) {
                throw new DomsObjectNotFoundException("Expected exactly 1 identifier from DOMS, got " + objects.size()
                        + " for object with DCIdentifier: '" + path + "'. Don't know where to add file.");
            }
            String fileObjectPid = objects.get(0);
            String url = baseUrl + job.getIngestableFile().getFileID();
            efedora.addExternalDatastream(fileObjectPid, CONTENTS, job.getIngestableFile().getFileID(), url, "application/octet-stream",
                    PDF_MIMETYPE, null, "Adding file after bitrepository ingest");
            Date dsAdded = new Date();
            log.trace("It took {} ms to add external datastream to doms for path '{}'", dsAdded.getTime() - objFound.getTime(), path);
            String checksum = Base16Utils.decodeBase16(job.getIngestableFile().getChecksum().getChecksumValue());
            efedora.addRelation(fileObjectPid, "info:fedora/" + fileObjectPid + "/" + CONTENTS, RELATION_PREDICATE,
                    checksum, true, "Adding checksum after bitrepository ingest");
            Date finished = new Date();
            log.trace("It took {} ms to add relation in doms for path '{}'", finished.getTime() - dsAdded.getTime(), path);
            log.trace("In total it took {} ms to register file in doms for path '{}'", finished.getTime() - start.getTime(), path);
*/


        /*if (event.getEventType().equals(OperationEvent.OperationEventType.COMPLETE)) {
            PutJob job = getJob(event);
            if (job != null) {
                domsRegister.registerJp2File(job);
            }
        }*/


        } catch (Exception e) {

        }
    }


/*
    @Override
    public void handleEvent(OperationEvent event) {
        if (event.getEventType().equals(OperationEvent.OperationEventType.COMPLETE)) {
            PutJob job = getJob(event);
            if(job != null) {
                log.info("Completed ingest of file " + event.getFileID());
                domsRegister.registerJp2File(job);
                operationLimiter.removeJob(job);
            } else {
                log.warn("Failed to find PutJob for file '{}' for event '{}'", event.getFileID(), event.getEventType());
            }
        } else if (event.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
            PutJob job = getJob(event);
            if(job != null) {
                log.warn("Failed to ingest file " + job.getIngestableFile().getFileID() + ", Cause: " + event);
                List<String> components = new ArrayList<String>();
                if(event instanceof OperationFailedEvent) {
                    OperationFailedEvent opEvent = (OperationFailedEvent) event;
                    List<ContributorEvent> events = opEvent.getComponentResults();
                    if(events != null) {
                        for(ContributorEvent contributorEvent : events) {
                            if(contributorEvent.getEventType().equals(OperationEvent.OperationEventType.COMPONENT_FAILED)) {
                                components.add(contributorEvent.getContributorID());
                            }
                        }
                    }
                }
                String failureDetails = "Failed conversation '" + event.getConversationID() 
                        + "' with reason: '" + event.getInfo() + "' for components: " +components;
                job.addResultMessage(failureDetails);
                failedJobs.add(job);
                operationLimiter.removeJob(job);
            } else {
                log.info("Failed to find PutJob for file '{}' for event '{}', skipping further handling", event.getFileID(), event.getEventType());
            }
        } else {
            log.debug("Got an event that I really don't care about, event type: '{}' for fileID '{}'", event.getEventType(), event.getFileID());
        }
    }*/

    private PutJob getJob(OperationEvent event) {
        PutJob job = null;
        job = operationLimiter.getJob(event.getFileID());
        return job;
    }
}
