package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.time.LocalDate.now;

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
     * and pass it into the given function returning a Tool, which is then executed.  It is not
     * expected to return.
     *
     * @param args     like passed in to "main(String[] args)"
     * @param function function creating a populated Tool from a configuration map.
     */

    public static void execute(String[] args, Function<ConfigurationMap, Tool> function) {
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

        // -- and go.
        execute(map, function);
    }

    /**
     * Expect a argument array (like passed in to "main(String[] args)"), create a configuration map from args[0], and
     * pass it into the given function returning a Tool, which is then executed.  It is not
     * expected to return.
     *
     * @param map      configuration map to pass into <code>function</code>
     * @param function function creating a populated Tool from a configuration map.
     */
    public static void execute(ConfigurationMap map, Function<ConfigurationMap, Tool> function) {
        Objects.requireNonNull(map, "map == null");
        final Logger log = LoggerFactory.getLogger(AutonomousPreservationToolHelper.class);

        log.info("*** Started at {} - {} ms since JVM start.", now(), getRuntimeMXBean().getUptime());
        log.debug("configuration: {}", map);

        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> log.info("*** Stopped at {} - {} ms since JVM start.", now(), getRuntimeMXBean().getUptime()
                )));
        try {
            String result = function.apply(map).call();
            log.trace("Result: {}", result);
        } catch (Throwable e) {
            log.error("Runnable threw exception:", e);
        }
    }

}
