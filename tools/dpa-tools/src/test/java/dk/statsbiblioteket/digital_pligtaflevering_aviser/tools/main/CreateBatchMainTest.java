package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import org.junit.Test;

/**
 *
 */
public class CreateBatchMainTest {
    @Test
    public void createB20160811_RT1() {
        CreateBatchMain.main(new String[]{"create-batch-B20160811-RT1.properties"});
    }
}
