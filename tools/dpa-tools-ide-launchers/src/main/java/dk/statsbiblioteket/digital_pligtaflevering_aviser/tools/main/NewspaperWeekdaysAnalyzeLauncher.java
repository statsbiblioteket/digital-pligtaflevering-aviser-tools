package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;

import java.nio.file.Path;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.IngesterMain.DPA_DELIVERIES_FOLDER;

public class NewspaperWeekdaysAnalyzeLauncher {
    public static void main(String[] args) throws Exception {
        // for jaxb-impl 2.3.0 under Java 10 - https://github.com/javaee/jaxb-v2/issues/1197
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        Path batchPath = MavenProjectsHelper.getRequiredPathTowardsRoot(IngestDeliverySamplesLauncher.class, "delivery-samples");


        NewspaperWeekdaysAnalyzeMain.main(new String[]{
                "newspapers-on-weekdays-analyze.properties",
                DPA_DELIVERIES_FOLDER + "=" + batchPath.toAbsolutePath(),
        });
    }
}
