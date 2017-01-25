package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import java.net.URISyntaxException;

/**
 * Developer class for stating the class ValidateXMLMain
 * This is used for initiating the process of validating all xml that has been ingested into fedora.
 */
public class ValidateXMLMainTest {

    @org.junit.Test
    public void invocationTest() throws URISyntaxException {

        ValidateXMLMain.main(new String[]{
                "xmlvalidate-vagrant.properties",
        });
    }
}
