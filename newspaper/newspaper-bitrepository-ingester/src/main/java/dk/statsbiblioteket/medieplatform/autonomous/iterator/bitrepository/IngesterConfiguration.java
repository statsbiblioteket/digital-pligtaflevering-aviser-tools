package dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository;

import java.util.Properties;

import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;

public class IngesterConfiguration {
    public static final String COLLECTIONID_PROPERTY="bitrepository.ingester.collectionid";
    public static final String COMPONENTID_PROPERTY="bitrepository.ingester.componentid";
    public static final String SETTINGS_DIR_PROPERTY="bitrepository.ingester.settingsdir";
    public static final String CERTIFICATE_PROPERTY="bitrepository.ingester.certificate";
    public static final String URL_TO_BATCH_DIR_PROPERTY="bitrepository.ingester.urltobatchdir";
    public static final String MAX_NUMBER_OF_PARALLEL_PUTS_PROPERTY="bitrepository.ingester.numberofparrallelPuts";
    public static final String BITMAG_BASEURL_PROPERTY = "bitrepository.ingester.baseurl";
    public static final String FORCE_ONLINE_COMMAND = "bitrepository.ingester.forceOnlineCommand";
    public static final String DOMS_TIMEOUT = "bitrepository.ingester.domsTimeout";
    public static final String MAX_BITMAG_PUT_RETRIES = "bitrepository.ingester.maxPutRetries";
    
    private final String componentID;
    private final String collectionID;
    private final String SettingsDir;
    private final String certificateLocation;
    private final int maxNumberOfParallelPuts;
    private final String domsUrl;
    private final String domsUser;
    private final String domsPass;
    private final String bitmagBaseUrl;
    private final String forceOnlineCommand;
    private final String pidgeneratorurl;
    private final int fedoraRetries;
    private final int delayBetweenFedoraRetries;
    private final int maxThreads;
    private final String urlToBatchDir;
    private final long domsTimeout;
    private final int maxBitmagRetries;

    public IngesterConfiguration(Properties properties) {
        componentID = properties.getProperty(COMPONENTID_PROPERTY);
        collectionID = properties.getProperty(COLLECTIONID_PROPERTY);
        SettingsDir = properties.getProperty(SETTINGS_DIR_PROPERTY);
        certificateLocation = properties.getProperty(SETTINGS_DIR_PROPERTY) + "/" + properties.getProperty(CERTIFICATE_PROPERTY);
        maxNumberOfParallelPuts = Integer.parseInt(properties.getProperty(MAX_NUMBER_OF_PARALLEL_PUTS_PROPERTY));
        domsUrl = properties.getProperty(ConfigConstants.DOMS_URL);
        domsUser = properties.getProperty(ConfigConstants.DOMS_USERNAME);
        domsPass = properties.getProperty(ConfigConstants.DOMS_PASSWORD);
        bitmagBaseUrl = properties.getProperty(BITMAG_BASEURL_PROPERTY);
        forceOnlineCommand = properties.getProperty(FORCE_ONLINE_COMMAND);
        pidgeneratorurl = properties.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL);
        fedoraRetries = Integer.parseInt(properties.getProperty(ConfigConstants.FEDORA_RETRIES, "1"));
        delayBetweenFedoraRetries = Integer.parseInt(properties.getProperty(ConfigConstants.FEDORA_DELAY_BETWEEN_RETRIES, "100"));
        maxThreads = Integer.parseInt(properties.getProperty(ConfigConstants.THREADS_PER_BATCH, "1"));
        urlToBatchDir = properties.getProperty(URL_TO_BATCH_DIR_PROPERTY);
        domsTimeout = Long.parseLong(properties.getProperty(DOMS_TIMEOUT, "3600000"));
        maxBitmagRetries = Integer.parseInt(properties.getProperty(MAX_BITMAG_PUT_RETRIES, "3"));
    }

    public String getComponentID() {
        return componentID;
    }

    public String getSettingsDir() {
        return SettingsDir;
    }

    public String getCertificateLocation() {
        return certificateLocation;
    }

    public int getMaxNumberOfParallelPuts() {
        return maxNumberOfParallelPuts;
    }

    public String getCollectionID() {
        return collectionID;
    }
    
    public String getDomsUrl() {
        return domsUrl;
    }
    
    public String getDomsUser() {
        return domsUser;
    }
    
    public String getDomsPass() {
        return domsPass;
    }
    
    public String getBitmagBaseUrl() {
        return bitmagBaseUrl;
    }
    
    public String getForceOnlineCommand() {
        return forceOnlineCommand;
    }

    public String getPidgeneratorurl() {
        return pidgeneratorurl;
    }

    public int getFedoraRetries() {
        return fedoraRetries;
    }

    public int getDelayBetweenFedoraRetries() {
        return delayBetweenFedoraRetries;
    }
    
    public int getMaxThreads() {
        return maxThreads;
    }

    public String getUrlToBatchDir() {
        return urlToBatchDir;
    }

    public long getDomsTimeout() {
        return domsTimeout;
    }

    public int getMaxBitmagRetries() {
        return maxBitmagRetries;
    }
}
