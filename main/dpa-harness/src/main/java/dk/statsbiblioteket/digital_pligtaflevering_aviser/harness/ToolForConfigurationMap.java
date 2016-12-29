package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

/**
 * This holds the configuration map to Tool code - typically a dagger invocation.  It used
 * to be provided directly in the invocation but in order to reuse it elsewhere we need it in a
 * method.
 */
public interface ToolForConfigurationMap {
    Tool getTool(ConfigurationMap configurationMap);
}
