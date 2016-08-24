package dk.statsbiblioteket.newspaper.bitrepository.ingester;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;

import org.bitrepository.common.utils.Base16Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.PutJob;

/**
 * Handle the registration of the bit repository URL for a given JP2000 file in DOMS.  
 */
public class DomsJP2FileUrlRegister implements AutoCloseable {
    private final Logger log = LoggerFactory.getLogger(getClass());
    public static final String JP2_MIMETYPE = "image/jp2";
    public static final String RELATION_PREDICATE = "http://doms.statsbiblioteket.dk/relations/default/0/1/#hasMD5";
    public static final String CONTENTS = "CONTENTS";

    private final Batch batch;
    private EnhancedFedora enhancedFedora;
    private final String baseUrl;
    private final ResultCollector resultCollector;
    private final ExecutorService pool;
    private long timeout;

    /**
     * Constructor
     * @param batch
     * @param central The EnhancedFedora used for registering the objects in DOMS.
     * @param baseUrl The base of the URL where the files can be accessed.
     * @param resultCollector The ResultCollector in which to register failures.
     * @param maxThreads the maximum number of threads used for registering objects in DOMS.
     * @param timeout
     */
    public DomsJP2FileUrlRegister(Batch batch, EnhancedFedora central, String baseUrl, ResultCollector resultCollector,
                                  int maxThreads, long timeout) {
        this.batch = batch;
        this.enhancedFedora = central;
        this.baseUrl = baseUrl;
        this.resultCollector = resultCollector;
        this.timeout = timeout;

        this.pool = Executors.newFixedThreadPool(maxThreads, new ThreadFactory() {
            private ThreadFactory threadFactory = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = threadFactory.newThread(r);
                if (!thread.isDaemon()) {
                    thread.setDaemon(true);
                }
                return thread;
            }
        });
    }

    /**
     * Register the location of a file in the DOMS object identified by path. 
     * @param job The PutJob that containing the file that should be registered in DOMS
     * @throws java.util.concurrent.RejectedExecutionException if the threadpool is already shutdown and just waiting for already submitted tasks to complete
     */
    public void registerJp2File(PutJob job) throws RejectedExecutionException{
        pool.submit(new RegistrationTask(job));
    }

    @Override
    public void close() throws InterruptedException {
        try {
            pool.shutdown();
            pool.awaitTermination(timeout, TimeUnit.MILLISECONDS);
        } finally {
            if (!pool.isTerminated()){
                log.error("Doms ingest of batch {} not done after '{}'. Stopping the doms ingester forcibly",batch.getFullID(),timeout);
                resultCollector.addFailure(batch.getFullID(),
                                                  "Exception",
                                                  getClass().getSimpleName(),
                                                  "Doms ingest not done after '" + timeout + "'. Stopping the doms ingester forcibly");
            }
            pool.shutdownNow();
        }
    }

    private class RegistrationTask implements Runnable {
        private final PutJob job;

        public RegistrationTask(PutJob job) {
            this.job = job;
        }

        @Override
        public void run() {
            try {
                register();
            } catch (DomsObjectNotFoundException e) {
                log.error("Failed to find the proper object in DOMS for '"+job.getIngestableFile().getPath()+"'", e);
                resultCollector.addFailure(job.getIngestableFile().getFileID(), "exception", getClass().getSimpleName(),
                        "Could not find the proper DOMS object to register the ingested file to: " + e.toString());
            } catch (Exception e) {
                log.error("Failed to register the url for '"+job.getIngestableFile().getPath()+"' in DOMS", e);
                resultCollector.addFailure(job.getIngestableFile().getFileID(),
                                                  "exception",
                                                  getClass().getSimpleName(),
                                                  "Failed to update DOMS object with the ingested file: " + e.toString());
            }
        }

        private void register() throws DomsObjectNotFoundException, BackendInvalidCredsException, BackendMethodFailedException, 
                BackendInvalidResourceException {
            List<String> objects;
            Date start = new Date();
            String path = job.getIngestableFile().getPath();
            objects = enhancedFedora.findObjectFromDCIdentifier(path);
            Date objFound = new Date();
            log.trace("It took {} ms to find object in doms for path '{}'", objFound.getTime() - start.getTime(), path);
            if(objects.size() != 1) {
                throw new DomsObjectNotFoundException("Expected exactly 1 identifier from DOMS, got " + objects.size()
                        + " for object with DCIdentifier: '" + path + "'. Don't know where to add file.");
            }
            String fileObjectPid = objects.get(0);
            String url = baseUrl + job.getIngestableFile().getFileID();
            enhancedFedora.addExternalDatastream(fileObjectPid, CONTENTS, job.getIngestableFile().getFileID(), url, "application/octet-stream", 
                    JP2_MIMETYPE, null, "Adding file after bitrepository ingest");
            Date dsAdded = new Date();
            log.trace("It took {} ms to add external datastream to doms for path '{}'", dsAdded.getTime() - objFound.getTime(), path);
            String checksum = Base16Utils.decodeBase16(job.getIngestableFile().getChecksum().getChecksumValue());
            enhancedFedora.addRelation(fileObjectPid, "info:fedora/" + fileObjectPid + "/" + CONTENTS, RELATION_PREDICATE,
                    checksum, true, "Adding checksum after bitrepository ingest");
            Date finished = new Date();
            log.trace("It took {} ms to add relation in doms for path '{}'", finished.getTime() - dsAdded.getTime(), path);
            log.trace("In total it took {} ms to register file in doms for path '{}'", finished.getTime() - start.getTime(), path);

        }

    }

}