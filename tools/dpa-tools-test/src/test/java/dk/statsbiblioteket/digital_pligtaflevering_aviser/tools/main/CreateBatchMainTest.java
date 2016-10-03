package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import org.junit.Test;

/**
 *
 */
public class CreateBatchMainTest {
    @Test
    public void createB20160811_RT1() {
        CreateBatchMain.main(new String[]{
                "create-batch.properties",
                "createbatch.batchid=20160913",
                "createbatch.roundtrip=1",
                "autonomous.agent=register-batch-trigger"});
    }
}
