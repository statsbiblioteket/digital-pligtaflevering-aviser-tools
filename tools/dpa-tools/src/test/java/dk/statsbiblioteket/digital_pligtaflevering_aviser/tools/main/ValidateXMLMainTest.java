package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.JaxbList;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.PdfContentDelegate;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;
import org.junit.Before;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test af xml validation against xsd.
 * This component uses two different xsd-files Article.xsd and PdfInfo.xsd.
 * It looks for the rootTag in the xml, and finds the schema expected to match the name of the rootTag
 */
public class ValidateXMLMainTest {

    private ValidateXMLMain.ValidateXMLModule xmlValidatorModule;

    // e.g. Creating an similar object and share for all @Test
    @Before
    public void runBeforeTestMethod() {
        xmlValidatorModule = new ValidateXMLMain.ValidateXMLModule();
    }


    @org.junit.Test
    public void analyzeXmlForFileList() throws Exception {

        ArrayList<String> files = new ArrayList<String>();
        files.add("embeddedFile1");
        files.add("embeddedFile2");
        JaxbList jaxbList = new JaxbList(files);

        byte[] yy = PdfContentDelegate.processListOfEmbeddedFilesToBytestream().apply(jaxbList);

        String jaxbStream = PdfContentDelegate.marshallListOfEmbeddedFilesInfo(jaxbList).toString();

        JaxbList newJaxbList = PdfContentDelegate.getListOfEmbeddedFilesFromXml(jaxbStream);


        assertEquals("Failed Files : ", "embeddedFile1", newJaxbList.getList().get(0));
        assertEquals("Failed Files : ", "embeddedFile2", newJaxbList.getList().get(1));


    }


    // @org.junit.Test
    // FIXME:  This test assumes that all deliveries in source tree are _valid_.  Fails if adding more.  FIX by making an integration test.
    public void analyzeDeliveriesFolderTest() throws Exception {

        String folder = getBatchFolder();
        boolean[] allOk = new boolean[]{true};
        List<String> failedFilePaths = new ArrayList<>();
        Files.walk(Paths.get(folder), FileVisitOption.FOLLOW_LINKS)
                .filter(p -> p.toString().endsWith(".xml"))
                .forEach(filePath -> {
                    if (Files.isRegularFile(filePath)) {
                        try {
                            String xsdName = xmlValidatorModule.getRootTagName(new InputSource(Files.newInputStream(filePath)));
                            Map<String, String> xsdMap = xmlValidatorModule.provideXsdRootMap();
                            URL xsdUrl = getClass().getClassLoader().getResource(xsdMap.get(xsdName));
                            BufferedReader in = new BufferedReader(new InputStreamReader(Files.newInputStream(filePath), "UTF8"));
                            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                            Schema schema = schemaFactory.newSchema(xsdUrl);
                            Validator validator = schema.newValidator();
                            validator.validate(new StreamSource(in));
                            // xmlValidatorModule.log.trace("Validation of the xml-content is accepted");
                        } catch (Exception e) {
                            // This exception is not kept since this exception should just result in registrating that the xml is not validate
                            xmlValidatorModule.log.info("not valid: " + filePath, e);
                            allOk[0] = false;
                            failedFilePaths.add(filePath.toString());
                        }
                    }
                });
        assertEquals("Failed Files : " + failedFilePaths, true, allOk[0]);
    }

    @org.junit.Test
    public void analyzeAcceptedXMLArticleTest() throws Exception {
        assertEquals("Test of approved article", true, validatePath("xmlValidation/articleCorrectTest.xml"));
    }

    @org.junit.Test
    public void analyzeAcceptedXMLPageTest() throws Exception {
        assertEquals("Test of approved page", true, validatePath("xmlValidation/pageCorrectTest.xml"));
    }

    @org.junit.Test
    public void analyzeFailingXMLArticleTest() throws Exception {
        assertEquals("Test of failing article", false, validatePath("xmlValidation/articleFailTest.xml"));
    }

    @org.junit.Test
    public void analyzeFailingXMLPageTest() throws Exception {
        assertEquals("Test of failing page", false, validatePath("xmlValidation/pageFailTest.xml"));
    }

    /**
     * Check wether a path to an xml-file can be validated aganst xsd-files
     *
     * @param path
     * @return
     * @throws Exception
     */
    private boolean validatePath(String path) throws Exception {
        URL xmlurl = getClass().getClassLoader().getResource(path);
        String xsdName = xmlValidatorModule.getRootTagName(new InputSource(xmlurl.getFile()));
        Map<String, String> xsdMap = xmlValidatorModule.provideXsdRootMap();
        URL xsdUrl = getClass().getClassLoader().getResource(xsdMap.get(xsdName));
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(xsdUrl);
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(in));
            xmlValidatorModule.log.trace("Validation of the xml-content is accepted");
            return true;
        } catch (IOException | SAXException e) {
            //This exception is not kept since this exception should just result in registrating that the xml is not validate
            xmlValidatorModule.log.info("Validation of the xml-content is rejected");
            return false;
        }
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
        Path batchPath = MavenProjectsHelper.getRequiredPathTowardsRoot(startDir, batchDirPathInWorkspace);
        return batchPath.toString();
    }

}
