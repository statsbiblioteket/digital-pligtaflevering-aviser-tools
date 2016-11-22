package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.newspaper.CreateBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PIDGENERATOR_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER;

/**
 * Unfinished create batch trigger main.
 */
public class CreateBatchMain {
    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerCreateBatchMain_CreateBatchComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, CreateBatchModule.class})
    interface CreateBatchComponent {
        Tool getTool();
    }

    /**
     * Create an empty file to indicate that the batch has been run
     * @param file
     * @throws IOException
     */
    public static void touch(File file) throws IOException{
        if (!file.exists()) {
            new FileOutputStream(file).close();
        }
        file.setLastModified(System.currentTimeMillis());
    }



    @Module
    static class CreateBatchModule {
        public static final String AUTONOMOUS_AGENT = "autonomous.agent";

        Logger log = LoggerFactory.getLogger(this.getClass());


        @Provides
        Tool provideTool(@Named(AUTONOMOUS_AGENT) String premisAgent,
                         @Named(DOMS_URL) String domsUrl,
                         @Named(DOMS_USERNAME) String domsUser,
                         @Named(DOMS_PASSWORD) String domsPass,
                         @Named(DOMS_PIDGENERATOR_URL) String urlToPidGen,
                         @Named(ITERATOR_FILESYSTEM_BATCHES_FOLDER) String batchFolder) {


            try {

                //Look in filesystem and find all deliveries that has not yet been seen by "CreateBatch"
                File[] directories = new File(batchFolder).listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory()/* && !Paths.get(file.getPath(), file.getName()).toFile().exists()*/;
                    }
                });

                //Iterate through the deliveries
                for (File batchItem : directories) {
                    //touch(Paths.get(batchItem.getAbsolutePath(), batchItem.getName()).toFile());

                    //TODO: quick and dirty way of splitting up the batchname
                    String batchName = batchItem.getName();
                    String batchIdValue = batchName.substring(3, 11);
                    String roundtripValue = batchName.substring(14);

                    CreateBatch.main(new String[]{batchIdValue, roundtripValue, premisAgent, domsUrl, domsUser, domsPass, urlToPidGen, batchFolder});
                    //touch(Paths.get(batchItem.getAbsolutePath(), batchItem.getName()).toFile());
                }

            } catch (Exception e) {
                log.error("Failed creating delivery", e.fillInStackTrace());
            }

            return () -> {
                return "created batch for deliveries";
            };
        }

        @Provides
        @Named(AUTONOMOUS_AGENT)
        String provideAgent(ConfigurationMap map) {
            return map.getRequired(AUTONOMOUS_AGENT);
        }
    }
}
