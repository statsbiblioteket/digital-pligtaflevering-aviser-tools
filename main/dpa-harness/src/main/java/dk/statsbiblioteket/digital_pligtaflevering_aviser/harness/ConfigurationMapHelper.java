package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;

/**
 *
 */
public class ConfigurationMapHelper {
    public static ConfigurationMap configurationMapFromPropertyFile(String configurationFileName) {
        File configurationFile = new File(configurationFileName);
        try {
            return configurationMapFromPropertyFile0(configurationFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(configurationFile.getAbsolutePath() + " not found", e);
        } catch (IOException e) {
            throw new RuntimeException(configurationFile.getAbsolutePath() + " cannot be read", e);
        }
    }

    protected static ConfigurationMap configurationMapFromPropertyFile0(File configurationFile) throws IOException {
        ConfigurationMap map = new ConfigurationMap(Collections.emptyMap());
        map.addPropertyFile(new FileReader(configurationFile));
        return map;
    }
}
