package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import org.junit.Before;
import org.xml.sax.InputSource;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test af xml validation against xsd
 */
public class ValidateXMLMainTest {

    private ValidateXMLMain.ValidateXMLModule xmlValidatorModule;


    // Should rename to @BeforeTestMethod
    // e.g. Creating an similar object and share for all @Test
    @Before
    public void runBeforeTestMethod() {
        xmlValidatorModule = new ValidateXMLMain.ValidateXMLModule();
    }


    @org.junit.Test
    public void analyzeDeliveriesFolderTest() throws Exception {

        String folder = getBatchFolder();
        Files.walk(Paths.get(folder)).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                String file = filePath.getFileName().toString();
                if (file.endsWith(".xml")) {
                    try {

                        String xsdName = xmlValidatorModule.getRootName(new InputSource(filePath.toString()));
                        Map<String, String> xsdMap = xmlValidatorModule.provideXsdRootMap();
                        URL xsdUrl = getClass().getClassLoader().getResource("xmlValidation/" + xsdMap.get(xsdName));
                        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath.toString()), "UTF8"));
                        assertEquals("Failed Files : " + filePath.toString(), true, xmlValidatorModule.tryParsing(in, xsdUrl));

                    } catch (Exception e) {
                        assertEquals(e.getMessage(), true, false);
                    }

                }
            }
        });
    }

    @org.junit.Test
    public void analyzeAcceptedXMLArticleTest() throws Exception {
        URL xmlurl = getClass().getClassLoader().getResource("xmlValidation/articleTest.xml");
        ValidateXMLMain.ValidateXMLModule xmlValidatorModule = new ValidateXMLMain.ValidateXMLModule();
        String xsdName = xmlValidatorModule.getRootName(new InputSource(xmlurl.getFile()));
        Map<String, String> xsdMap = xmlValidatorModule.provideXsdRootMap();
        URL xsdUrl = getClass().getClassLoader().getResource("xmlValidation/" + xsdMap.get(xsdName));
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        assertEquals("Test of approved article", true, xmlValidatorModule.tryParsing(in, xsdUrl));
    }


    @org.junit.Test
    public void analyzeAcceptedXMLPageTest() throws Exception {

        URL xmlurl = getClass().getClassLoader().getResource("xmlValidation/pageTest.xml");
        String xsdName = xmlValidatorModule.getRootName(new InputSource(xmlurl.getFile()));
        Map<String, String> xsdMap = xmlValidatorModule.provideXsdRootMap();
        URL xsdUrl = getClass().getClassLoader().getResource("xmlValidation/" + xsdMap.get(xsdName));
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        assertEquals("Test of approved page", true, xmlValidatorModule.tryParsing(in, xsdUrl));
    }

    @org.junit.Test
    public void analyzeFailingXMLArticleTest() throws Exception {

        URL xmlurl = getClass().getClassLoader().getResource("xmlValidation/articleFailTest.xml");
        String xsdName = xmlValidatorModule.getRootName(new InputSource(xmlurl.getFile()));
        Map<String, String> xsdMap = xmlValidatorModule.provideXsdRootMap();
        URL xsdUrl = getClass().getClassLoader().getResource("xmlValidation/" + xsdMap.get(xsdName));
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        assertEquals("Test of failing article", false, xmlValidatorModule.tryParsing(in, xsdUrl));
    }


    @org.junit.Test
    public void analyzeFailingXMLPageTest() throws Exception {

        URL xmlurl = getClass().getClassLoader().getResource("xmlValidation/pageFailTest.xml");
        String xsdName = xmlValidatorModule.getRootName(new InputSource(xmlurl.getFile()));
        Map<String, String> xsdMap = xmlValidatorModule.provideXsdRootMap();
        URL xsdUrl = getClass().getClassLoader().getResource("xmlValidation/" + xsdMap.get(xsdName));
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        assertEquals("Test of failing page", false, xmlValidatorModule.tryParsing(in, xsdUrl));
    }


    /**
     * Get the folder where the testbatches is located during test in dev-environment
     *
     * @return
     */
    private String getBatchFolder() {
        String batchDirPathInWorkspace = "delivery-samples";

        // http://stackoverflow.com/a/320595/53897
        URI l = null;
        try {
            l = CreateDeliveryMain.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Path startDir = Paths.get(l);

        // Look for the first instance of batchDir in the directories towards the root of the file system.
        // This will work anywhere in the source tree.  StreamEx provide three argument iterate() in Java 8.
        Path batchPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(startDir, batchDirPathInWorkspace);
        return batchPath.toString();
    }

}