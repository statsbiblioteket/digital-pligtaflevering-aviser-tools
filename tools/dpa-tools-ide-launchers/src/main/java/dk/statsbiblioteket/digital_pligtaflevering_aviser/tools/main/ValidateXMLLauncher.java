package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

/**
 * Developer class for stating the class ValidateXMLMain
 * This is used for initiating the process of validating all xml that has been ingested into fedora.
 */
public class ValidateXMLLauncher {

    public static void main(String[] args) throws Exception {
        ValidateXMLMain.main(new String[]{
                "xmlvalidate.properties",
        });
    }
}
