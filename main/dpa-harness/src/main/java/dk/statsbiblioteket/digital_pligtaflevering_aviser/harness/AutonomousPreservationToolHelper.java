package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p> This is the entry point in the scaffolding.  Reads in a configuration map (exact way depends on the method
 * called) and invokes a <code>Function&lt;ConfigurationMap, Runnable></code> responsible for generating a Runnable
 * configured using the ConfigurationMap.  The typical use case is invoking a generated Dagger builder, configure it
 * using the map and ask it for a Runnable. </p> <p>When launched from an appassembler generated script the "app.name"
 * property contains the name of the program.</p>
 */
public class AutonomousPreservationToolHelper {
    /**
     * Expect a argument array (like passed in to "main(String[] args)"), create a configuration map from args[0], and
     * pass it into the given function returning a AutonomousPreservationTool, which is then executed.  It is not
     * expected to return.
     *
     * @param args     like passed in to "main(String[] args)"
     * @param function function creating a populated AutonomousPreservationTool from a configuration map.
     */

    public static void execute(String[] args, Function<ConfigurationMap, AutonomousPreservationTool> function) {
        Objects.requireNonNull(args, "args == null");
        if (args.length < 1) {
            throw new IllegalArgumentException("required argument: configuration file/url");
        }
        ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties(args[0]);
        // ["config.properties",  "a=1", "b=2", "c=3"]
        Map<String, String> argsMap = Arrays.stream(args)
                .skip(1)
                .map(s -> s.split(Pattern.quote("="), 2)) // ["a","1"]
                .filter(a -> a.length > 1) // only use those with "=" in
                .collect(Collectors.toMap(a -> a[0], a -> a[1])); // map.add("a", "1")
        map.addMap(argsMap);
        execute(map, function);
    }

    /**
     * Expect a argument array (like passed in to "main(String[] args)"), create a configuration map from args[0], and
     * pass it into the given function returning a AutonomousPreservationTool, which is then executed.  It is not
     * expected to return.
     *
     * @param map      configuration map to pass into <code>function</code>
     * @param function function creating a populated AutonomousPreservationTool from a configuration map.
     */
    public static void execute(ConfigurationMap map, Function<ConfigurationMap, AutonomousPreservationTool> function) {
        Objects.requireNonNull(map, "map == null");
        function.apply(map).execute();
    }
}
