package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.LoggingFaultBarrier;

import java.util.Objects;
import java.util.function.Function;

/**
 *
 */
public class AutonomousPreservationTool  {
    public static void execute(String[] args, Function<ConfigurationMap, Runnable> function) {
        if (Objects.requireNonNull(args, "args == null").length < 1) {
            throw new IllegalArgumentException("required argument: configuration file/url");
        }
        ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties(args[0]);
        execute(map, function);
    }

    public static void execute(ConfigurationMap map, Function<ConfigurationMap, Runnable> function) {
        Objects.requireNonNull(map, "map == null");
        new LoggingFaultBarrier(function.apply(map)).run();
    }
}
