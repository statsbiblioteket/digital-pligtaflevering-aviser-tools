package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 *
 */
public class CreateBatchMain {
    public static void main(String[] args) {
        args = new String[]{"createbatch.properties"}; // FIXME: properly.
        AutonomousPreservationTool.execute(
                args,
                m -> DaggerCreateBatchMain_CreateBatchTaskComponent.builder().configurationMap(m).build().getTask()
        );
    }

    @Singleton // FIXME
    @Component(modules = {ConfigurationMap.class, DomsModule.class, CreateBatchModule.class})
    interface CreateBatchTaskComponent extends TaskComponent {
    }

    @Singleton // FIXME
    @Module
    class CreateBatchModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Runnable provideTask() {
            return () -> {
            };
        }
    }
}
