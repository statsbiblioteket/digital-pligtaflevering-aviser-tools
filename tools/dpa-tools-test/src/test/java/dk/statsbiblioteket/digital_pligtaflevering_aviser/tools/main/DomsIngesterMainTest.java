package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import org.junit.Test;

/**
 *
 */
public class DomsIngesterMainTest {
    @Test
    public void domsIngestB20160811_RT1() {
        DomsIngesterMain.main(new String[]{
                "doms-ingester.properties",
                "createbatch.batchid=20160913",
                "createbatch.roundtrip=1",
                "autonomous.agent=register-batch-trigger"});
    }
}
