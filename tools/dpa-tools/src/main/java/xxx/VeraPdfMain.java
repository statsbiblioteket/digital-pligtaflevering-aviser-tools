package xxx;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.LoggingFaultBarrier;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class VeraPdfMain {
    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("What", "Yes!");

        VeraPdfTaskComponent taskComponent = DaggerVeraPdfTaskComponent.builder()
                .configurationMap(new ConfigurationMap(map))
                .build();

        Runnable runnable = taskComponent.getRunnableTask();
        new LoggingFaultBarrier(runnable).run();
    }
}

@Singleton // FIXME
@Component(modules = {ConfigurationMap.class, VeraPdfModule.class})
interface VeraPdfTaskComponent { //extends TaskComponent<DomsItem, String> {
    Runnable getRunnableTask();
};

@Singleton // FIXME
@Module
class VeraPdfModule {

    @Provides
    Runnable provideRunnable(@Named("What") String what) {
        return () -> {
            System.out.println("Hello " + what);
        };
    }

    @Provides
    @Named("What")
    String getWhat(ConfigurationMap map) {
        return map.get("What");
    }
}
