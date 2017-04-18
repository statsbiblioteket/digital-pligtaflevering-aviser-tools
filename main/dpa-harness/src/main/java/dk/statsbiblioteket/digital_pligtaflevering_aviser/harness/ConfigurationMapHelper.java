package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Objects;

/**
 * ConfigurationMapHelper has helper methods to create a configuration map from external resources.
 */
public class ConfigurationMapHelper {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMapHelper.class);

    /**
     * Loads a configuration map from the given configuration location.  First the location is considered as
     * a file name and loaded from there and returned if found, next as a resource name and loaded from there
     * and returned if found.  Otherwise an exception is thrown.1
     * @param configurationLocation file name/resource name of properties to read.
     * @return populated map with configuration strings.
     */
    public static ConfigurationMap configurationMapFromProperties(String configurationLocation) {

        ConfigurationMap map = new ConfigurationMap(Collections.emptyMap());

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
