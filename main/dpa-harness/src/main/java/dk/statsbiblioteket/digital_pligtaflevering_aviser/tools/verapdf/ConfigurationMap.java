package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.verapdf;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;


/**
 * <p>
 * ConfigurationMap holds a map of string to string (i.e. the general form
 * of java properties).  The toString() method list the keys in alphabetical order.
 * </p>
 */
public class ConfigurationMap extends TreeMap<String, String> {

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
     * EventAdderValuePutter those environment variables with the provided keys that actually exist (value != null) to the configuration map.
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
     * Buffers the reader, reads in the entries, and add them to the configuration map.  The reader is closed afterwards.  Values (but not keys) are trimmed.
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
}

