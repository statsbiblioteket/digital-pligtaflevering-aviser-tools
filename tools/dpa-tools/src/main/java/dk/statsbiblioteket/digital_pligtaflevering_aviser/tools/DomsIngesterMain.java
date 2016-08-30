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
public class DomsIngesterMain {
    public static void main(String[] args) {
        args = new String[]{"domsingester.properties"}; // FIXME: properly.
        AutonomousPreservationTool.execute(
                args,
                m -> DaggerDomsIngesterMain_DomsIngesterComponent.builder().configurationMap(m).build().getTask()
        );
    }

    @Singleton // FIXME
    @Component(modules = {ConfigurationMap.class, DomsModule.class, DomsIngesterModule.class})
    protected interface DomsIngesterComponent extends TaskComponent {
    }

    @Singleton // FIXME
    @Module
    protected class DomsIngesterModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Runnable provideTask() {
            return () -> {};
        }
    }
}
