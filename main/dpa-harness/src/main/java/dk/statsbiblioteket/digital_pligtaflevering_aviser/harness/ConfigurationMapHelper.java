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
 *
 */
public class ConfigurationMapHelper {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMapHelper.class);

    public static ConfigurationMap configurationMapFromProperties(String configurationLocation) {
        ConfigurationMap map = new ConfigurationMap(Collections.emptyMap());

        File configurationFile = new File(configurationLocation);
        try {
            map.addPropertyFile(new FileReader(configurationFile));
            LOGGER.debug("read file {}: {}", configurationFile.getAbsolutePath(), map);
            return map;
        } catch (FileNotFoundException e) {
            // fall through if not present!
        } catch (IOException e) {
            throw new RuntimeException(configurationFile.getAbsolutePath() + " cannot be read", e);
        }

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configurationLocation);
        Objects.requireNonNull(stream, configurationLocation + " is not a valid file nor a valid resource");

        try {
            map.addPropertyFile(new InputStreamReader(stream));
            LOGGER.debug("read resource {}: {}", configurationLocation, map);
            return map;
        } catch (IOException e) {
            throw new RuntimeException("resource " + configurationLocation + " cannot be read", e);
        }
    }
}
