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

    @Module
    static class CreateDeliveryModule {
        public static final String AUTONOMOUS_AGENT = "autonomous.agent";

        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Tool provideTool(@Named(AUTONOMOUS_AGENT) String premisAgent,
                         @Named(DOMS_URL) String domsUrl,
                         @Named(DOMS_USERNAME) String domsUser,
                         @Named(DOMS_PASSWORD) String domsPass,
                         @Named(DOMS_PIDGENERATOR_URL) String urlToPidGen,
                         @Named(ITERATOR_FILESYSTEM_BATCHES_FOLDER) String batchFolder) {

            return () -> {
                // Look in filesystem and find all deliveries that has not yet been seen by "CreateDelivery"
                File[] directories = new File(batchFolder).listFiles(File::isDirectory);
                final String batchResponse = "";
                //Iterate through the deliveries
                for (File deliveryItemDirectory : directories) {
                    Pattern pattern = Pattern.compile("^(.*)_rt([0-9]+)$");
                    Matcher matcher = pattern.matcher(deliveryItemDirectory.getName());
                    if (matcher.matches()) {
                        String batchIdValue = matcher.group(1);
                        String roundtripValue = matcher.group(2);
                        log.trace("create delivery for {}, (batch id: {}, round trip: {})", deliveryItemDirectory, batchIdValue, roundtripValue);
                        CreateDelivery.main(new String[]{batchIdValue, roundtripValue, premisAgent, domsUrl, domsUser, domsPass, urlToPidGen, batchFolder});
                    } else {
                        log.trace("did not match, skipping {}", deliveryItemDirectory.getName());
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
    }
}
