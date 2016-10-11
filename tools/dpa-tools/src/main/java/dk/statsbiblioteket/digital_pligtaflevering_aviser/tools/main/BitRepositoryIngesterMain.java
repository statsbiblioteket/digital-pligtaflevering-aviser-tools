package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.AutonomousPreservationToolComponent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.CommonModule;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.CallResult;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperBatchAutonomousComponentUtils;
import dk.statsbiblioteket.medieplatform.autonomous.RunnableComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 *
 */
public class BitRepositoryIngesterMain {
    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerBitRepositoryIngesterMain_BitRepositoryIngesterDaggerComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, BitRepositoryIngesterModule.class})
    protected interface BitRepositoryIngesterDaggerComponent extends AutonomousPreservationToolComponent {
    }

    @Module
    protected static class BitRepositoryIngesterModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Runnable provideTask(ConfigurationMap configurationMap) {
            return () -> {
                // Ensure that these properties are defined
                configurationMap.getRequired("dpa.testmode");

                Properties properties = configurationMap.asProperties();
                RunnableComponent<Batch> component = new dk.statsbiblioteket.newspaper.bitrepository.ingester.BitrepositoryIngesterComponent(properties);

                CallResult<Batch> result = NewspaperBatchAutonomousComponentUtils.startAutonomousComponent(properties, component);
                log.debug("result was: " + result);
            };
        }
    }
}
