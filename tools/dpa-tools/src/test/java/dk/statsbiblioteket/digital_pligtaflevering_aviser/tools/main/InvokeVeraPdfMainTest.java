package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

/**
 *
 */

public class InvokeVeraPdfMainTest {
    @org.junit.Test
    public void invocationTest() {
        InvokeVeraPdfMain.main(new String[]{"invoke-verapdf-vagrant.properties"});
    }
}
