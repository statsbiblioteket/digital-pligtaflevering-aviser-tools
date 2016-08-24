package dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository;

import java.util.Collection;

/**
 * Exception class to carry unfinished jobs in case of timeouts 
 */
public class NotFinishedException extends Exception {
    private Collection<PutJob> unfinishedJobs;

    public NotFinishedException(Collection<PutJob> unfinishedJobs) {
        this.unfinishedJobs = unfinishedJobs;
    }

    public Collection<PutJob> getUnfinishedJobs() {
        return unfinishedJobs;
    }

    @Override
    public String toString() {
        return "NotFinishedException [unfinishedJobs=" + unfinishedJobs + "]";
    }
}
