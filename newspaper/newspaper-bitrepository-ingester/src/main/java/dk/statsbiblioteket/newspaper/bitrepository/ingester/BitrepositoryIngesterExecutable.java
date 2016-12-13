package dk.statsbiblioteket.newspaper.bitrepository.ingester;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.CallResult;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperBatchAutonomousComponentUtils;
import dk.statsbiblioteket.medieplatform.autonomous.RunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/** AutonomousComponent wrapper for the BitrepositoryIngester. */
public class BitrepositoryIngesterExecutable {
    private static Logger log = LoggerFactory.getLogger(BitrepositoryIngesterExecutable.class);

    /**
     * The class must have a main method, so it can be started as a command line tool
     *
     * @param args the arguments.
     *
     * @throws Exception
     * @see NewspaperBatchAutonomousComponentUtils#parseArgs(String[])
     */
    public static void main(String... args) throws IOException {
        System.exit(doMain(args));
    }

    /**
     * Main method, so it can be started as a command line tool.
     *
     * @param args the arguments.
     *
     * @throws Exception
     * @see NewspaperBatchAutonomousComponentUtils#parseArgs(String[])
     */
    public static int doMain(String[] args) throws IOException {
        log.info("Starting with args {}", (Object []) args);
        Properties properties = readProperties(args);
        RunnableComponent<Batch> component = new BitrepositoryIngesterComponent(properties);

        CallResult<Batch> result = NewspaperBatchAutonomousComponentUtils.startAutonomousComponent(properties, component);
        log.debug("result was: " + result);
        return result.containsFailures();
        
    }

    /**
     * Sample method to parse properties. This is probably not the best way to do this
     * It makes a new properties, with the system defaults. It then scan the args for a the string "-c". If found
     * it expects the next arg to be a path to a properties file.
     *
     * @param args the command line args
     *
     * @return as a properties
     * @throws java.io.IOException if the properties file could not be read
     */
    public static Properties parseArgs(String[] args) throws IOException {
        Properties properties = new Properties(System.getProperties());
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-c")) {
                String configFileName = args[i + 1];
                properties.load(new FileInputStream(configFileName));
                File configFile = new File(configFileName);
                properties.setProperty(
                        IngesterConfiguration.SETTINGS_DIR_PROPERTY,
                        configFile.getParentFile().getAbsolutePath());
            }
        }
        return properties;
    }

    /**
     * Reads the properties from the arguments or system properties. Either the first argument must be a path to a
     * properties file, or, if not, the system property "newspaper.component.properties.file" must denote such a path.
     * If neither, then a runtime exception is set
     *
     * @param args the command line arguments
     *
     * @return a properties object parsed from the properties file
     * @throws IOException      if the file could not be read
     * @throws RuntimeException if no path could be determined
     */
    private static Properties readProperties(String[] args) throws IOException, RuntimeException {
        Properties properties = new Properties();
        String propsFileString;
        if (args.length >= 1) {
            propsFileString = args[0];
        } else {
            propsFileString = System.getProperty("newspaper.component.properties.file");
        }
        if (propsFileString == null) {
            throw new RuntimeException("Properties file must be defined either as command-line parameter or as system" + "property newspaper.component.properties .");
        }
        log.info("Reading properties from " + propsFileString);
        File propsFile = new File(propsFileString);
        if (!propsFile.exists()) {
            throw new FileNotFoundException("No such file: " + propsFile.getAbsolutePath());
        }
        properties.load(new FileReader(propsFile));
        return properties;
    }


    /**
     * Check that all the required properties are set
     *
     * @param props     the properties to check
     * @param propnames the names that must be found in the properties
     *
     * @throws RuntimeException if any name could not be found
     */
    private static void checkProperties(Properties props, String[] propnames) throws RuntimeException {
        for (String prop : propnames) {
            if (props.getProperty(prop) == null) {
                throw new RuntimeException("Property not found: " + prop);
            }
        }
    }
}
