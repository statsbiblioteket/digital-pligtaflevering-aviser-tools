package dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to carry the information relevant to a put job 
 */
public class PutJob {
    private int putAttempts = 0;
    private final IngestableFile ingestableFile;
    private final List<String> resultMessages = new ArrayList<>();

    public PutJob(IngestableFile ingestableFile) {
        this.ingestableFile = ingestableFile;
    }
    
    public void incrementPutAttempts() {
        putAttempts++;
    }
    
    public int getPutAttempts() {
        return putAttempts;
    }
    
    public IngestableFile getIngestableFile() {
        return ingestableFile;
    }

    public String toString() {
        return ingestableFile.getPath();
    }
    
    public void addResultMessage(String message) {
        resultMessages.add(message);
    }
    
    public List<String> getResultMessages() {
        return resultMessages;
    }
}
