package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import org.junit.Test;

/**
 *
 */
public class CreateBatchMainTest {
    @Test
    public void createB20160811_RT1() {
        // Look for delivery samples and invoke for each folder there
        CreateBatchMain.main(new String[]{
                "create-batch.properties",
                "createbatch.batchid=20160811",
                "createbatch.roundtrip=1",
                "autonomous.agent=register-batch-trigger"});
    }
}
