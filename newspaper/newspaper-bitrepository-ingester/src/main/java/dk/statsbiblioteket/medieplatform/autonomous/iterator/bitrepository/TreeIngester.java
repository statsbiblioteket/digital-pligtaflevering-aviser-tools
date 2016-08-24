package dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.JMSException;

import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.bitrepository.ingester.DomsJP2FileUrlRegister;
import dk.statsbiblioteket.util.Strings;

/**
 * Class handling ingest of a set of files in a tree iterator structure.
 */
public class TreeIngester implements AutoCloseable {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final long DEFAULT_FILE_SIZE = 0;
    private static final int SLEEP_PERIOD = 1000; /* One second in ms */
    private final IngestableFileLocator fileLocator;
    private final String collectionID;
    private final EventHandler handler;
    private final ParallelOperationLimiter parallelOperationLimiter;
    private final BlockingQueue<PutJob> failedJobsQueue = new LinkedBlockingQueue<>();
    private final PutFileClient putFileClient;
    private final ResultCollector resultCollector;
    private final int maxRetries;
    private final Batch batch;

    /**
     *
     * @param collectionID The collectionID of the collection to store the ingested files in.
     * @param timoutForLastOperation How many milliseconds should the ingester wait for the last files found in the tree
     *                               to finish ingesting before force quitting .
     * @param fileLocator Used for finding the relevant files.
     * @param putFileClient For handling the actual ingests.
     * @param resultCollector Failures are logged here.
     * @param maxNumberOfParallelPuts The number of puts to to perform in parallel.
     */
    public TreeIngester(String collectionID, ParallelOperationLimiter operationLimiter, DomsJP2FileUrlRegister domsRegistor, 
            IngestableFileLocator fileLocator, PutFileClient putFileClient, ResultCollector resultCollector, int maxRetries, Batch batch) {
        this.collectionID = collectionID;
        this.fileLocator = fileLocator;
        this.parallelOperationLimiter = operationLimiter;
        this.resultCollector = resultCollector;
        handler = new PutFileEventHandler(parallelOperationLimiter, failedJobsQueue, domsRegistor);
        this.putFileClient = putFileClient;
        this.maxRetries = maxRetries;
        this.batch = batch;
    }

    public void performIngest() throws InterruptedException {
        IngestableFile file = null;
        try {
            do {
                file = fileLocator.nextFile();
                try {
                    if (file != null) {
                            PutJob job = new PutJob(file);
                            putFile(job);
                    }
                } catch (Exception e) {
                    log.error("Failed to ingest file '{}'", file, e);
                    resultCollector.addFailure(file.getPath(), "jp2file", getClass().getSimpleName(), 
                            "Failed to ingest file. '" + e.toString() + "'");
                    
                }
            } while (file != null);
        } catch (Exception e ) {
            log.error("Failure while ingesting files", e);
            resultCollector.addFailure(batch.getFullID(), "exception", e.getClass().getSimpleName(), 
                    "Exception during ingest: " + e.toString(), Strings.getStackTrace(e));
        }
        while(!finished()) {
            retryPuts();
            Thread.sleep(SLEEP_PERIOD);
        }
    }
    
    /**
     * Retry PutJobs that have been registered for retry. 
     * Only retry jobs for which less than the allowed attempts have been done 
     * Reports jobs that have been retried too many times
     */
    private void retryPuts() {
        Set<PutJob> jobs = new HashSet<>();
        failedJobsQueue.drainTo(jobs);
        for(PutJob job : jobs) {
            if(job.getPutAttempts() < maxRetries) {
                putFile(job);
                log.info("Retrying file '{}' (attempt #{})", job.getIngestableFile().getFileID(), job.getPutAttempts());
            } else {
                log.info("Failing file '{}' after {} attempts", job.getIngestableFile().getFileID(), job.getPutAttempts());
                resultCollector.addFailure(job.getIngestableFile().getFileID(), "jp2file", 
                        getClass().getSimpleName(), job.getResultMessages().toString());
            }
        }
    }
    
    /**
     * Method to determine if we're done putting files. 
     */
    private boolean finished() {
        boolean finished = false;
        /* Ye be warned, the sequence of the '&&' matters.*/
        finished = (parallelOperationLimiter.isEmpty() && failedJobsQueue.isEmpty());
        return finished;
    }

    /**
     * Calls the concrete putFileClient blocking if the maxNumberOfParallelPut are exceeded.
     */
    private void putFile(PutJob job) {
        parallelOperationLimiter.addJob(job);
        job.incrementPutAttempts();
        putFileClient.putFile(collectionID,
                job.getIngestableFile().getLocalUrl(), job.getIngestableFile().getFileID(), DEFAULT_FILE_SIZE,
                job.getIngestableFile().getChecksum(), null, handler, null);
    }

    @Override
    public void close() {
        try {
            MessageBus messageBus = MessageBusManager.getMessageBus();
            if (messageBus != null) {
                MessageBusManager.getMessageBus().close();
            }
        } catch (JMSException e) {
            log.warn("Failed to shutdown messagebus connection", e);
        } catch (Exception e) {
            log.warn("Caught unexpected exception while closing messagebus down", e);
        }
    }
}
