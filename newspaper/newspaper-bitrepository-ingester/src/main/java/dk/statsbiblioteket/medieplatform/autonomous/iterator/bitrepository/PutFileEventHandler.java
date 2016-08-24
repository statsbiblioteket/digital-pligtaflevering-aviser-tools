package dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.newspaper.bitrepository.ingester.DomsJP2FileUrlRegister;

public class PutFileEventHandler implements EventHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ParallelOperationLimiter operationLimiter;
    private final DomsJP2FileUrlRegister domsRegister;
    private final BlockingQueue<PutJob> failedJobs;
    
    public PutFileEventHandler(ParallelOperationLimiter putLimiter, BlockingQueue<PutJob> failedJobsQueue, 
            DomsJP2FileUrlRegister domsRegister) {
    	this.operationLimiter = putLimiter;
        this.domsRegister = domsRegister;
        this.failedJobs = failedJobsQueue;
    }

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
    }

    private PutJob getJob(OperationEvent event) {
        PutJob job = null;
        job = operationLimiter.getJob(event.getFileID());
        return job;
    }
}
