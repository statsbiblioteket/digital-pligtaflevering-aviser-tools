package xxx;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;

import java.util.stream.Stream;

/**
 *
 */
public class  VeraPdfMain {
}

@Component(modules = {ConfigurationMap.class, VeraPdfModule.class})
interface VeraPdfTaskComponent { //extends TaskComponent<DomsItem, String> {
};
@Module
class VeraPdfModule {
    @Provides
    Stream<Stream<DomsItem>> provideDomsItemStream() {
        //return Stream.of(Stream.of("Item 1", "Item 2"));
        return Stream.of(Stream.of());
    }
}
