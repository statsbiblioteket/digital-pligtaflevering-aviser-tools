package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ToolForConfigurationMap;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PIDGENERATOR_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER;

/**
 * Unfinished create batch trigger main.
 */
public class CreateDeliveryMain implements ToolForConfigurationMap {
    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(args, new CreateDeliveryMain());
    }

    @Override
    public Tool getTool(ConfigurationMap configurationMap) {
        return DaggerCreateDeliveryMain_CreateDeliveryComponent.builder().configurationMap(configurationMap).build().getTool();
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, CreateDeliveryModule.class})
    interface CreateDeliveryComponent {
        Tool getTool();
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
                         @Named(ITERATOR_FILESYSTEM_BATCHES_FOLDER) String deliveryFolderName,
                         @Named(AUTONOMOUS_DONEDIR) String doneDir) {

            //Look in filesystem and find all deliveries that has not yet been seen by "CreateDelivery"
            File[] directories = new File(deliveryFolderName).listFiles(File::isDirectory);

            return () -> {
                // Iterate through the deliveries on disk
                for (File deliveryItemDirectory : directories) {
                    Pattern pattern = Pattern.compile("^(.*)_rt([0-9]+)$");
                    Matcher matcher = pattern.matcher(deliveryItemDirectory.getName());
                    if (matcher.matches()) {
                        String deliveryIdValue = matcher.group(1);
                        String roundtripValue = matcher.group(2);

                        final File directoryProcessedIndicatorFile = new File(doneDir, deliveryFolderName);

                        if (!directoryProcessedIndicatorFile.exists()) {
                            CreateDelivery.main(new String[]{deliveryIdValue, roundtripValue, premisAgent, domsUrl, domsUser, domsPass, urlToPidGen, deliveryFolderName});
                            touch(directoryProcessedIndicatorFile);
                        } else {
                            log.trace("already processed, skipping {}", deliveryItemDirectory.getName());
                        }
                    } else {
                        log.trace("file name did not match, skipping {}", deliveryItemDirectory.getName());
                    }
                }
                String joinedString = StringUtils.join(directories, " ");

                return "created delivery for " + joinedString;
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

    /**
     * Create an empty file to indicate that the batch has been run.  Last modified time is set to now.
     * A simple implementation of the Unix "touch" command.
     *
     * @param file file to "touch"
     * @throws IOException
     */
    public static void touch(File file) throws IOException {
        if (!file.exists()) {
            new FileOutputStream(file).close();
        }
        file.setLastModified(System.currentTimeMillis());
    }

}
