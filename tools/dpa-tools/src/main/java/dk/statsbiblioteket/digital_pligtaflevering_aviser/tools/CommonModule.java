package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools;

import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;

import javax.inject.Named;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.LoggingFaultBarrier.JVM_DUMPHEAP;

/**
 *
 */
@Module
public class CommonModule {
    @Provides
    @Named(JVM_DUMPHEAP)
    boolean getJvmDumpheap(ConfigurationMap map) {
        return Boolean.parseBoolean(map.get(JVM_DUMPHEAP));
    }
}
