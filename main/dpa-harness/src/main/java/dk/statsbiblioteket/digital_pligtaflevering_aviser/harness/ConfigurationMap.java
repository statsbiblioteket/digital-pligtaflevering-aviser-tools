package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import dagger.Module;
import dagger.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * <p> ConfigurationMap holds a map of string to string (i.e. the general form of java properties) and can be used
 * directly as a Dagger 2 module.  The toString() method list the keys in alphabetical order. FIXME:  Full technical
 * explanation pending.</p>
 */
@Module
public class ConfigurationMap {

    final Logger log = LoggerFactory.getLogger(ConfigurationMap.class);

    Map<String, String> map = new TreeMap<>();

    Set<String> everUsed = new HashSet<>();

    // Constructor must take arguments to ensure Dagger does not instantiate automatically.

    public ConfigurationMap(Map<String, String> initialMap) {
        map.putAll(Objects.requireNonNull(initialMap, "initialMap == null"));

        // During development this helps in slimming down configuration files.
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    List<String> unused = map.keySet().stream()
                            .filter(e -> everUsed.contains(e) == false)
                            .sorted()
                            .collect(Collectors.toList());
                    if (unused.size() > 0 ) {
                        System.err.println("Unused configuration keys: " + unused);
                    }
                }
        ));

    }

    /**
     * Provider method needed to give Dagger a hook into accessing the configuration map.
     *
     * @return current configuration map
     */
    @Provides
    // @Singleton - FIXME, we only want a single, read-only copy.
    public ConfigurationMap getConfigurationMap() {
        return this;
    }

    /**
     * Get configuration map as properties - this is currently for interfacing
     * with legacy code.
     */

    public Properties asProperties() {
        Properties p = new Properties() {
            @Override
            public String getProperty(String key) {
                String property = super.getProperty(key);
                log.trace("Property: {} = {}", key, property);
                return property;
            }

            @Override
            public String getProperty(String key, String defaultValue) {
                String property = super.getProperty(key, defaultValue);
                log.trace("Property: {} = {} (default {})", key, property, defaultValue);
                return property;
            }
        };
        p.putAll(map);
        return p;
    }

    /**
     * Adds those system properties with the provided keys that actually exist (value != null) to the configuration map.
     * Non-existing values silently ignored.
     */

    public void addSystemProperties(String... propertyKeys) {
        for (String key : propertyKeys) {
            String value = System.getProperty(key);
            if (value != null) {
                map.put(key, value);
            }
        }
    }

    /**
     * EventAdderValuePutter those environment variables with the provided keys that actually exist (value != null) to
     * the configuration map. Non-existing values silently ignored.
     */

    public void addEnvironmentVariables(String... variableKeys) {
        for (String key : variableKeys) {
            String value = System.getenv(key);
            if (value != null) {
                map.put(key, value);
            }
        }
    }

    /**
     * Buffers the reader, reads in the entries, and add them to the configuration map.  The reader isn't closed
     * afterwards.  Values (but not keys) are trimmed.
     */

    public void addPropertyFile(Reader reader) throws IOException {
        Properties p = new Properties();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            p.load(bufferedReader);
        }
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String value = String.valueOf(entry.getValue()).trim();
            map.put(key, value);
        }
    }

    /**
     * Adds all entries in mapToAdd to the current configuration map
     */

    public void addMap(Map<String, String> mapToAdd) {
        map.putAll(mapToAdd);
    }

    /**
     * getRequired(key) returns the same as get(key) but throws a ConfigurationKeyNotSetException if the key is not
     * present in the map.  This is to make @Provider methods simpler for the "key must be set"-case.
     *
     * @param key key to get value for
     * @return value if present, throws exception if not.
     */
    public String getRequired(String key) {
        everUsed.add(key);
        if (map.containsKey(key)) {
            return map.get(key);
        }
        throw new ConfigurationKeyNotSetException(key);
    }

    /**
     * returns the value for the key in the map if present.  If not, defaultValue is returned
     */

    public String getDefault(String key, String defaultValue) {
        everUsed.add(key);
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return defaultValue;
    }

    /**
     * getRequiredInt returns a configuration map entry as a string.  If the
     * value stored for the key is not a valid integer, a meaningful message is returned.
     *
     * @param key configuration key
     * @return value stored in map converted with Integer.parseInt()
     */
    public int getRequiredInt(String key) {
        everUsed.add(key);
        String value = getRequired(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("key:" + key + " - invalid integer: " + value);
        }
    }

    /**
     * toString() is overwritten to ensure that keys with "password" are shown as "***" instead of their
     * actual value.  Adapted from the AbstractMap implementation.
     *
     * @return Normal Map toString() but with password values given as "***"
     */
    @Override
    public String toString() {
        // Adapted from AbstractMap
        Iterator<Map.Entry<String, String>> i = map.entrySet().iterator();
        if (!i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            Map.Entry<String, String> e = i.next();
            String key = e.getKey();
            String value = e.getValue();
            sb.append(key);
            sb.append('=');
            sb.append(key.contains("password") ? "***" : value);
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }
}
