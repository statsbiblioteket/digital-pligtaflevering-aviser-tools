package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.achernar;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.ValidateXMLMain;

/**
 * Developer class for stating the class ValidateXMLMain
 * This is used for initiating the process of validating all xml that has been ingested into fedora.
 */
public class ValidateXMLAchernarLauncher {

    public static void main(String[] args) throws Exception {
        System.setProperty(ConfigurationMapHelper.DPA_DEFAULT_CONFIGURATION, "achernar.properties");
        ValidateXMLMain.main(new String[]{
                "achernar/xmlvalidate-achernar.properties",
        });
    }
}
