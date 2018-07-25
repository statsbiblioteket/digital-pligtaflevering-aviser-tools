package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

/**
 * Developer class for stating the class ValidateXMLMain
 * This is used for initiating the process of validating all xml that has been ingested into fedora.
 */
public class ValidateXMLLauncher {

    public static void main(String[] args) throws Exception {
        // for jaxb-impl 2.3.0 under Java 10 - https://github.com/javaee/jaxb-v2/issues/1197
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        ValidateXMLMain.main(new String[]{
                "xmlvalidate.properties",
        });
    }
}
