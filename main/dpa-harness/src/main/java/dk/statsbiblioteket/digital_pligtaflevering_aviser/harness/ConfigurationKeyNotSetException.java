package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

/**
 *
 */
public class ConfigurationKeyNotSetException extends RuntimeException {
    public ConfigurationKeyNotSetException(String key) {
        super(key);
    }
}
