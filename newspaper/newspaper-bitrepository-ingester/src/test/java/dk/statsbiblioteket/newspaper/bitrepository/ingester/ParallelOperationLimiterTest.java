package dk.statsbiblioteket.newspaper.bitrepository.ingester;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.net.MalformedURLException;
import java.net.URL;

import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngestableFile;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.ParallelOperationLimiter;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.PutJob;


public class ParallelOperationLimiterTest {

    public final static String TEST_FILE_ID1 = "file1";
    public final static String TEST_FILE_ID2 = "file2";
    
    @Test
    public void putGetRemoveTest() throws MalformedURLException {
        ParallelOperationLimiter limiter = new ParallelOperationLimiter(2);
        PutJob theJob = new PutJob(new IngestableFile(TEST_FILE_ID1, new URL("file:/"), null, 0L, "/"));
        limiter.addJob(theJob);
        
        PutJob returnedJob = limiter.getJob(TEST_FILE_ID1);
        assertEquals(returnedJob, theJob, "The returned job and the original job should be the same");
        
        limiter.removeJob(returnedJob);
        
        PutJob secondReturnedJob = limiter.getJob(TEST_FILE_ID1);
        assertNull(secondReturnedJob, "Should get a null when the job is removed");
    }
    
    public void jobNotFoundTest() throws MalformedURLException {
        ParallelOperationLimiter limiter = new ParallelOperationLimiter(2);
        PutJob theJob = new PutJob(new IngestableFile(TEST_FILE_ID1, new URL("file:/"), null, 0L, "/"));
        limiter.addJob(theJob);
        
        PutJob nonExistingJob = limiter.getJob(TEST_FILE_ID2);
        assertNull(nonExistingJob, "The job should be null as it does not exist");
    }

}
