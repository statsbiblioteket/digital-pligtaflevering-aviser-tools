package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import one.util.streamex.StreamEx;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * <p> This is the entry point in the scaffolding.  Reads in a configuration map (exact way depends on the method
 * called) and invokes a <code>Function&lt;ConfigurationMap, Runnable></code> responsible for generating a Runnable
 * configured using the ConfigurationMap.  The typical use case is invoking a generated Dagger builder, configure it
 * using the map and ask it for a Runnable. </p> <p>When launched from an appassembler generated script the "app.name"
 * property contains the name of the program.</p>
 */
public class AutonomousPreservationToolHelper {
    /**
     * Expect a argument array (like passed in to "main(String[] args)"), create a configuration map from the
     * configuration file/resource denoted by args[0], plus the remaining arguments interpreted as "key=value" lines,
     * and pass it into the given function returning a AutonomousPreservationTool, which is then executed.  It is not
     * expected to return.
     *
     * @param args     like passed in to "main(String[] args)"
     * @param function function creating a populated AutonomousPreservationTool from a configuration map.
     */

    public static void execute(String[] args, Function<ConfigurationMap, AutonomousPreservationTool> function) {
        Objects.requireNonNull(args, "args == null");
        // args: ["config.properties",  "a=1", "b=2", "c=3"]
        if (args.length < 1) {
            throw new IllegalArgumentException("required argument: configuration file/url");
        }

        // "config.properties"
        String configurationFileName = args[0];

        ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties(configurationFileName);

        // remainingArgs: ["a=1", "b=2", "c=3"]
        String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
        Map<String, String> argsMap = new TreeMap<>();
        for (String keyValue : remainingArgs) {
            String[] splitKeyValue = keyValue.split(Pattern.quote("="), 2);
            if (splitKeyValue.length > 1) {
                String key = splitKeyValue[0];
                String value = splitKeyValue[1];
                argsMap.put(key, value);
            }
        }

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
        LoggerFactory.getLogger(AutonomousPreservationToolHelper.class).debug("configuration: {}", map);
        function.apply(map).execute();
    }

    /**
     * A launcher may have to locate a given path in a project. We traverse from startDir to the root using getParent()
     * looking for the path.  If not found, throw runtime exception.
     *
     * @param startPath  path where to start
     * @param pathToFind relative path name which must resolve from current path
     * @return
     */
    public static Path getRequiredPathTowardsRoot(Path startPath, String pathToFind) {
        return StreamEx // to get 3-arg iterate before Java 9
                .iterate(startPath, p -> p != null, p -> p.getParent()) // walk up to root
                .map(p -> p.resolve(pathToFind))
                .filter(p -> p.toFile().exists())
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(pathToFind + " not found towards root of " + startPath.toAbsolutePath())
                );
    }
}
