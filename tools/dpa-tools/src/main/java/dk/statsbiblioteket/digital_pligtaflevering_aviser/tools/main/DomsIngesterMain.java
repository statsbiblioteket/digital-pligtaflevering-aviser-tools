package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.DomsModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.AutonomousPreservationToolComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * Unfinished
 */
public class DomsIngesterMain {
    public static void main(String[] args) {
        args = new String[]{"domsingester.properties"}; // FIXME: properly.
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerDomsIngesterMain_DomsIngesterComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Singleton // FIXME
    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, DomsIngesterModule.class})
    protected interface DomsIngesterComponent extends AutonomousPreservationToolComponent {
    }

    @Singleton // FIXME
    @Module
    protected class DomsIngesterModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Runnable provideTask() {
            return () -> {
            };
        }
    }
}
