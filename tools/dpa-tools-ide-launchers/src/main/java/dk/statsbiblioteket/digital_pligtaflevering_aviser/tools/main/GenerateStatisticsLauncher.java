package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

/**
 * Developer class for stating the class ValidateXMLMain
 * This is used for initiating the process of validating all xml that has been ingested into fedora.
 */
public class GenerateStatisticsLauncher {

    public static void main(String[] args) throws Exception {

        GenerateStatisticsMain.main(new String[]{
                "statistics-vagrant.properties",
        });
    }
}
