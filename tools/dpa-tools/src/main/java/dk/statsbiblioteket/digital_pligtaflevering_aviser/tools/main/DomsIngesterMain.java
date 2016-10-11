package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.AutonomousPreservationToolComponent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.DomsModule;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.medieplatform.autonomous.CallResult;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperBatchAutonomousComponentUtils;
import dk.statsbiblioteket.medieplatform.autonomous.RunnableComponent;
import dk.statsbiblioteket.sbutil.webservices.authentication.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.util.Properties;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_LOCKSERVER_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_MAXTHREADS;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_SBOI_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PIDGENERATOR_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.FEDORA_DELAY_BETWEEN_RETRIES;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.FEDORA_RETRIES;

/**
 * Unfinished
 */
public class DomsIngesterMain {
    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerDomsIngesterMain_DomsIngesterComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, DomsIngesterModule.class})
    protected interface DomsIngesterComponent extends AutonomousPreservationToolComponent {
    }

    @Module
    protected static class DomsIngesterModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Runnable provideTask(@Named(DOMS_USERNAME) String domsUserName,
                             @Named(DOMS_PASSWORD) String domsPassword,
                             @Named(DOMS_URL) String fedoraLocation,
                             @Named(DOMS_PIDGENERATOR_URL) String domsPidgeneratorUrl,
                             @Named(FEDORA_RETRIES) int fedoraRetries,
                             @Named(FEDORA_DELAY_BETWEEN_RETRIES) int fedoraDelayBetweenRetries,
                             ConfigurationMap configurationMap)
        {
            // Adapted from PromptDomsIngesterComponent.doWork(...)
            return () -> {
                // Ensure that these properties are defined as legacy code needs them to be present.
                configurationMap.getRequired(AUTONOMOUS_MAXTHREADS);
                configurationMap.getRequired(AUTONOMOUS_LOCKSERVER_URL);
                configurationMap.getRequired(AUTONOMOUS_SBOI_URL);

                // -- ok, go

                Credentials creds = new Credentials(domsUserName, domsPassword);

                EnhancedFedoraImpl eFedora;
                try {
                    eFedora = new EnhancedFedoraImpl(
                            creds,
                            fedoraLocation,
                            domsPidgeneratorUrl,
                            null, fedoraRetries, fedoraDelayBetweenRetries);
                } catch (JAXBException | PIDGeneratorException | MalformedURLException e) {
                    throw new RuntimeException("EnhancedFedoraImpl constructor failed");
                }

                Properties properties = configurationMap.asProperties();
                RunnableComponent component = null; //new RunnablePromptDomsIngester(properties, eFedora);
                CallResult result = NewspaperBatchAutonomousComponentUtils.startAutonomousComponent(properties, component);

                log.info("result was: " + result);
            };
        }
    }
}
