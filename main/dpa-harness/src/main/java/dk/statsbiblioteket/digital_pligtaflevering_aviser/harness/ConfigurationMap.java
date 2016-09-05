package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

/**
 * <p> ConfigurationMap holds a map of string to string (i.e. the general form of java properties) and can be used
 * directly as a Dagger 2 module.  The toString() method list the keys in alphabetical order. FIXME:  Full technical
 * explanation pending.</p>
 */
@Module
public class ConfigurationMap extends TreeMap<String, String> {

    // Constructor must take arguments to ensure Dagger does not instantiate automatically.

    public ConfigurationMap(Map<String, String> initialMap) {
        this.putAll(Objects.requireNonNull(initialMap, "initialMap == null"));
    }

    @Provides
    @Singleton
    public ConfigurationMap getConfigurationMap() {
        return this;
    }

    /**
     * Adds those system properties with the provided keys that actually exist (value != null) to the configuration map.
     */

    public void addSystemProperties(String... propertyKeys) {
        for (String key : propertyKeys) {
            String value = System.getProperty(key);
            if (value != null) {
                this.put(key, value);
            }
        }
    }

    /**
     * EventAdderValuePutter those environment variables with the provided keys that actually exist (value != null) to
     * the configuration map.
     */

    public void addEnvironmentVariables(String... variableKeys) {
        for (String key : variableKeys) {
            String value = System.getenv(key);
            if (value != null) {
                this.put(key, value);
            }
        }
    }

    /**
     * Buffers the reader, reads in the entries, and add them to the configuration map.  The reader is closed
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
            this.put(key, value);
        }
    }

    /**
     * toString() is overwritten to ensure that keys with "password" are shown as "***" instead of their
     * actual value.  Adapted from the AbstractMap implementation.
     *
     * @return Normal Map toString() but with password values given as "***"
     */
    @Override
    public String toString() {  // Adapted from AbstractMap
        Iterator<Map.Entry<String, String>> i = entrySet().iterator();
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

