package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Objects;

/**
 * ConfigurationMapHelper has helper methods to create a configuration map from external resources.
 */
public class ConfigurationMapHelper {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMapHelper.class);
    /**
     * This is the name of the system property to contain the default configuration file name.  If relative,
     * it is to the users home directory.  Use this mechanism to store user name and passwords for servers other
     * than the vagrant running on localhost.
     */
    public static final String DPA_DEFAULT_CONFIGURATION = "dpa.defaultConfiguration";

    /**
     * Loads a configuration map from the given configuration location and an optional default configuration from
     * outside the source tree (this is intended for providing passwords outside the source tree).
     * <p>
     * <ul> <li>Start with an empty map.</li>
     * <p>
     * <li>If the system property DPA_DEFAULT_CONFIGURATION is provided, it contains a property filename (either
     * absolute or relative to the users home directory) which is loaded into the map if found (if not, an
     * IllegalArgumentException is thrown).  Put user names and passwords here for public code! </li>
     * <p>
     * <li>First consider the configurationLocation as a property file name and add its contents to the map if found,
     * and return.</li>
     * <p>
     * <li>Then consider the configurationLocation as a resource on the classpath containing properties.  If present,
     * add its contents to the map and return.</li>
     * <p>
     * <li>Otherwise an exception is thrown.</li> </ul>
     * <p>
     * Note:  The reason that a system property is used instead of implementing an include-mechanism in the
     * property file reading, was because it would be non-trivial to add variable expansion (of e.g. $HOME).
     *
     * @param configurationLocation file name/resource name of properties to read.
     * @return populated map with configuration strings.
     */
    public static ConfigurationMap configurationMapFromProperties(String configurationLocation) {

        ConfigurationMap map = new ConfigurationMap(Collections.emptyMap());

        // If a default configuration file was specified, read it first.  Put passwords here!

        String defaultConfigurationFileName = System.getProperty(DPA_DEFAULT_CONFIGURATION, "").trim();

        if (defaultConfigurationFileName != null && defaultConfigurationFileName.length() > 0) {
            File defaultConfigurationFile = new File(System.getProperty("user.home", "."), defaultConfigurationFileName);
            if (defaultConfigurationFile.exists()) {
                try (Reader reader = new BufferedReader(new FileReader(defaultConfigurationFile))) {
                    map.addPropertyFile(reader);
                } catch (IOException e) {
                    throw new RuntimeException(defaultConfigurationFile.getAbsolutePath(), e);
                }
            } else {
                throw new IllegalArgumentException("default configuration file specified, but not found: "
                        + defaultConfigurationFileName + " ("
                        + defaultConfigurationFile.getAbsolutePath() + ")"
                );
            }
        }

        // look for configuration file.

        File configurationFile = new File(configurationLocation);
        try {
            LOGGER.trace("read file {}: {}", configurationFile.getAbsolutePath(), map);
            FileReader fileReader = new FileReader(configurationFile);
            map.addPropertyFile(fileReader);
            fileReader.close();
            return map;
        } catch (FileNotFoundException e) {
            // fall through if not present!
        } catch (IOException e) {
            throw new RuntimeException(configurationFile.getAbsolutePath() + " cannot be read", e);
        }

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configurationLocation);
        Objects.requireNonNull(stream, configurationLocation + " is not a valid file nor a valid resource");

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(stream);
            map.addPropertyFile(inputStreamReader);
            inputStreamReader.close();
            LOGGER.trace("read resource {}: {}", configurationLocation, map);
            return map;
        } catch (IOException e) {
            throw new RuntimeException("resource " + configurationLocation + " cannot be read", e);
        }
    }
}
