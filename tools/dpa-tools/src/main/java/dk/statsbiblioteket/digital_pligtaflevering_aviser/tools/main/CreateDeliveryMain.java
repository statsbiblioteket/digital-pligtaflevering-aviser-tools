package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.newspaper.CreateDelivery;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PIDGENERATOR_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER;

/**
 * Unfinished create batch trigger main.
 */
public class CreateDeliveryMain {
    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerCreateDeliveryMain_CreateDeliveryComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, CreateDeliveryModule.class})
    interface CreateDeliveryComponent {
        Tool getTool();
    }


    /**
     * Create an empty file to indicate that the batch has been run
     * @param file
     * @throws IOException
     */
    public static void touch(File file) throws IOException {
        if (!file.exists()) {
            new FileOutputStream(file).close();
        }
        file.setLastModified(System.currentTimeMillis());
    }


    @Module
    static class CreateDeliveryModule {
        public static final String AUTONOMOUS_AGENT = "autonomous.agent";
        public static final String AUTONOMOUS_DONEDIR = "autonomous.filesystem.processed.deliverys";

        Logger log = LoggerFactory.getLogger(this.getClass());


        @Provides
        Tool provideTool(@Named(AUTONOMOUS_AGENT) String premisAgent,
                         @Named(DOMS_URL) String domsUrl,
                         @Named(DOMS_USERNAME) String domsUser,
                         @Named(DOMS_PASSWORD) String domsPass,
                         @Named(DOMS_PIDGENERATOR_URL) String urlToPidGen,
                         @Named(ITERATOR_FILESYSTEM_BATCHES_FOLDER) String deliveryFolder,
                         @Named(AUTONOMOUS_DONEDIR) String doneDir) {

            //Look in filesystem and find all deliveries that has not yet been seen by "CreateDelivery"
            File[] directories = new File(deliveryFolder).listFiles(File::isDirectory);

            return () -> {
                final String batchResponse = "";
                //Iterate through the deliveries
                for (File deliveryItem : directories) {
                    //quick and dirty way of splitting up the batchname
                    String deliveryFolderName = deliveryItem.getName();
                    String batchName = deliveryFolderName.replaceAll("[dl_]+[^-?0-9]+", " ");
                    String[] deliveryContent =  batchName.trim().split(" ");
                    String deliveryIdValue = deliveryContent[0];
                    String roundtripValue = deliveryContent[1];

                    File deliveryProcessedIndicator = new File(doneDir, deliveryFolderName);
                    if(!deliveryProcessedIndicator.exists()) {
                        CreateDelivery.main(new String[]{deliveryIdValue, roundtripValue, premisAgent, domsUrl, domsUser, domsPass, urlToPidGen, deliveryFolder});
                        touch(new File(doneDir, deliveryFolderName));
                    }
                }
                String joinedString = StringUtils.join(directories, " ");


                return "created batch for " + joinedString;
            };
        }

        @Provides
        @Named(AUTONOMOUS_AGENT)
        String provideAgent(ConfigurationMap map) {
            return map.getRequired(AUTONOMOUS_AGENT);
        }

        @Provides
        @Named(AUTONOMOUS_DONEDIR)
        String provideDoneDir(ConfigurationMap map) {
            return map.getRequired(AUTONOMOUS_DONEDIR);
        }
    }
}
