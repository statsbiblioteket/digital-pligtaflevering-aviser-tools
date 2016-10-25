package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.DomsModule;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.CallResult;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperBatchAutonomousComponentUtils;
import dk.statsbiblioteket.medieplatform.autonomous.RunnableComponent;
import dk.statsbiblioteket.newspaper.promptdomsingester.component.RunnablePromptDomsIngester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_LOCKSERVER_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_MAXTHREADS;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_SBOI_URL;

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
    protected interface DomsIngesterComponent {
        Tool getTool();
    }

    @Module
    protected static class DomsIngesterModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Tool provideTool(EnhancedFedora eFedora,
                             ConfigurationMap configurationMap) {
            // Adapted from PromptDomsIngesterComponent.doWork(...)
            return () -> {
                // Ensure that these properties are defined as legacy code needs them to be present.
                configurationMap.getRequired(AUTONOMOUS_MAXTHREADS);
                configurationMap.getRequired(AUTONOMOUS_LOCKSERVER_URL);
                configurationMap.getRequired(AUTONOMOUS_SBOI_URL);

                // -- ok, go
                Properties properties = configurationMap.asProperties();
                RunnableComponent component = new RunnablePromptDomsIngester(properties, eFedora);
                CallResult result = NewspaperBatchAutonomousComponentUtils.startAutonomousComponent(properties, component);

                log.info("result was: " + result);
            };
        }
    }
}
