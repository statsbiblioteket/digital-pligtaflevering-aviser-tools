package dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for holding running putjobs jobs
 * Makes it possible to limit the number of asynchronous jobs and share job objects between different threads
 */
public class ParallelOperationLimiter {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Map<String, PutJob> jobs = new ConcurrentHashMap<>(); 
    private Semaphore jobLimiter;

    public ParallelOperationLimiter(int limit) {
        jobLimiter = new Semaphore(limit);
    }

    /**
     * Will block until the if the activeOperations queue limit is exceeded and unblock when a job is removed.
     * @param job The job in the queue.
     */
    public void addJob(PutJob job) {
        jobLimiter.acquireUninterruptibly();
        jobs.put(job.getIngestableFile().getFileID(), job);

    }
    
    /**
     * Gets the PutJob for fileID
     * @param fileID The fileID to get the job for
     * @return PutJob the PutJob with relevant info for the job. May return null if no job matching fileID is found
     */
    public PutJob getJob(String fileID) {
        return jobs.get(fileID);
    }

    /**
     * Removes a job from the queue
     * @param job the PutJob to remove 
     */
    public void removeJob(PutJob job) {
        PutJob removedJob = jobs.remove(job.getIngestableFile().getFileID());
        if(removedJob != null) {
            jobLimiter.release();
        }
    }
    
    /**
     * Determine if there are no more jobs in the queue 
     */
     public boolean isEmpty() {
        return jobs.isEmpty();
    }
}
