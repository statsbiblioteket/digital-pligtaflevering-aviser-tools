package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

/**
 * Developer class for starting the class GenerateStatisticsMain
 * This is used for initiating the process of generating statistics of the content in a delivery.
 */
public class GenerateStatisticsLauncher {

    public static void main(String[] args) throws Exception {

        GenerateStatisticsMain.main(new String[]{
                "generate-statistics.properties",
        });
    }
}
