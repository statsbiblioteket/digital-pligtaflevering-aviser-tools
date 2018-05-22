package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

/**
 * Developer class for starting the class GenerateStatisticsMain
 * This is used for initiating the process of generating statistics of the content in a delivery.
 */
public class GenerateStatisticsLauncher {

    public static void main(String[] args) throws Exception {

        // for jaxb-impl 2.3.0 under Java 10 - https://github.com/javaee/jaxb-v2/issues/1197
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        GenerateStatisticsMain.main(new String[]{
                "generate-statistics.properties",
        });
    }
}
