package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.LoggingFaultBarrier;

import java.util.Objects;
import java.util.function.Function;

/**<p>
 * This is the entry point in the scaffolding.  Reads in a configuration map (exact way depends on the method called) and
 * invokes a <code>Function&lt;ConfigurationMap, Runnable></code> responsible for generating a Runnable configured using the
 * ConfigurationMap.  The typical use case is invoking a generated Dagger builder, configure it using the map and ask it for a Runnable.
 *</p>
 * <p>When launched from an appassembler generated script the "app.name" property contains the
 * name of the program.</p>
 */
public class AutonomousPreservationTool  {
    public static void execute(String[] args, Function<ConfigurationMap, LoggingFaultBarrier> function) {
        if (Objects.requireNonNull(args, "args == null").length < 1) {
            throw new IllegalArgumentException("required argument: configuration file/url");
        }
        ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties(args[0]);
        execute(map, function);
    }

    public static void execute(ConfigurationMap map, Function<ConfigurationMap, LoggingFaultBarrier> function) {
        Objects.requireNonNull(map, "map == null");
        function.apply(map).run();
    }
}
