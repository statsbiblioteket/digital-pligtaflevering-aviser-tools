package dk.statsbiblioteket.newspaper.bitrepository.ingester;

import java.io.FileInputStream;
import java.util.Properties;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.putfile.PutFileClient;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class BitrepositoryIngesterComponentIT {
    private final static String TEST_BATCH_ID = "400022028241";
    private String pathToConfig;
    private String pathToTestBatch;
    private final Properties properties = new Properties();
    /**
     * Tests that the ingester can parse a (small) production like batch.
     */
    @Test(groups = "integrationTest")
    public void smallBatchIngestCheck() throws Exception {
        properties.setProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER, pathToTestBatch + "/" + "small-test-batch");

        BitrepositoryIngesterComponent bitrepositoryIngesterComponent =
                new StubbedBitrepositoryIngesterComponent(properties);

        ResultCollector resultCollector = new ResultCollector("Bitrepository ingester", "v0.1", 1000);
        Batch batch = new Batch();
        batch.setBatchID(TEST_BATCH_ID);
        batch.setRoundTripNumber(1);

        bitrepositoryIngesterComponent.doWorkOnItem(batch, resultCollector);
        assertTrue(resultCollector.isSuccess());
    }

    /**
     * Tests that the ingester can parse a (small) production like batch.
     */
    @Test(groups = "integrationTest")
    public void forceOnlineFailure() throws Exception {
        properties.setProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER, pathToTestBatch + "/" + "small-test-batch");
        properties.setProperty(IngesterConfiguration.FORCE_ONLINE_COMMAND, "false");
        
        BitrepositoryIngesterComponent bitrepositoryIngesterComponent =
                new StubbedBitrepositoryIngesterComponent(properties);

        ResultCollector resultCollector = new ResultCollector("Bitrepository ingester", "v0.1", 1000);
        Batch batch = new Batch();
        batch.setBatchID(TEST_BATCH_ID);
        batch.setRoundTripNumber(1);

        bitrepositoryIngesterComponent.doWorkOnItem(batch, resultCollector);
        
        assertFalse(resultCollector.isSuccess());
        resultCollector.toReport().contains("Failed to force batch online.");
    }
    
    /**
     * Tests that the ingester can parse a (small) production like batch.
     */
    @Test(groups = "integrationTest")
    public void badBatchSurvivabilityCheck() throws Exception {
        properties.setProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER, pathToTestBatch + "/" + "bad-bad-batch");

        BitrepositoryIngesterComponent bitrepositoryIngesterComponent =
                new StubbedBitrepositoryIngesterComponent(properties);

        ResultCollector resultCollector = new ResultCollector("Mocked bitrepository ingester", "test version", 1000);
        Batch batch = new Batch();
        batch.setBatchID(TEST_BATCH_ID);
        batch.setRoundTripNumber(1);

        bitrepositoryIngesterComponent.doWorkOnItem(batch, resultCollector);
    }

    private class StubbedBitrepositoryIngesterComponent extends BitrepositoryIngesterComponent {
        PutFileClientStub clientStub = new PutFileClientStub();

        public StubbedBitrepositoryIngesterComponent(Properties properties) {
            super(properties);
        }

        @Override
        protected PutFileClient createPutFileClient(IngesterConfiguration configuration, Settings settings) {
            return clientStub;
        }

        @Override
        protected Settings loadSettings(IngesterConfiguration configuration) {
            return super.loadSettings(configuration);
        }
    }

    @BeforeMethod(alwaysRun = true)
    private void loadConfiguration() throws Exception {
        String generalPropertiesPath = System.getProperty("integration.test.newspaper.properties");
        String propertiesDir = generalPropertiesPath.substring(0, generalPropertiesPath.lastIndexOf("/"));
        pathToConfig = propertiesDir + "/newspaper-bitrepository-ingester-config";
        pathToTestBatch = System.getProperty("integration.test.newspaper.testdata");
        properties.load(new FileInputStream(pathToConfig + "/config.properties"));
        properties.setProperty(IngesterConfiguration.SETTINGS_DIR_PROPERTY, pathToConfig);
    }
}
