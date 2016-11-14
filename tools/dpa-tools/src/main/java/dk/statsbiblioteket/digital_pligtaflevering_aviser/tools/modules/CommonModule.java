package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules;

import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.LoggingFaultBarrier;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;

import javax.inject.Named;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.LoggingFaultBarrier.JVM_DUMPHEAP;

/**
 * Module containing providers for typical DPA Dagger dependencies.
 */
@Module
public class CommonModule {
    /**
     * Flag indicating whether the tool should ask the JVM to dump heap when exiting.
     *
     * @param map configuration map
     * @return boolean indicating if flag was set to "true".
     */
    @Provides
    @Named(JVM_DUMPHEAP)
    boolean getJvmDumpheapFlag(ConfigurationMap map) {
        return Boolean.parseBoolean(map.getDefault(JVM_DUMPHEAP, Boolean.FALSE.toString()));
    }

    /**
     * Dagger mapping from {@link Tool} interface to
     * {@link LoggingFaultBarrier} class.
     *
     * @param tool tool dependency which Dagger is to create to be able to invoke this provider.
     * @return
     */
//    @Provides
//    Tool getTool(LoggingFaultBarrier tool) {
//        return tool;
//    }
}
