package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import static org.junit.Assert.assertEquals;

/**
 * Developer class for stating the class ValidateXMLMain
 * This is used for initiating the process of validating all xml that has been ingested into fedora.
 */
public class ValidateXMLMainTest {



    @org.junit.Test
    public void analyzeAcceptedXMLArticleTest() throws Exception {

        URL xmlurl = getClass().getClassLoader().getResource("testResources/articleTest.xml");
        ValidateXMLMain.ValidateXMLModule xmlValidatorModule = new ValidateXMLMain.ValidateXMLModule();
        String xsdName = xmlValidatorModule.getRootName( new InputSource(xmlurl.getFile()));
        Map<String, String> xsdMap = xmlValidatorModule.provideXsdRootMap();
        URL xsdUrl = getClass().getClassLoader().getResource(xsdMap.get(xsdName));
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        assertEquals("Test of approved article", true, xmlValidatorModule.tryParsing(in, xsdUrl));
    }


    @org.junit.Test
    public void analyzeAcceptedXMLPageTest() throws Exception {

        URL xmlurl = getClass().getClassLoader().getResource("testResources/pageTest.xml");
        ValidateXMLMain.ValidateXMLModule xmlValidatorModule = new ValidateXMLMain.ValidateXMLModule();
        String xsdName = xmlValidatorModule.getRootName( new InputSource(xmlurl.getFile()));
        Map<String, String> xsdMap = xmlValidatorModule.provideXsdRootMap();
        URL xsdUrl = getClass().getClassLoader().getResource(xsdMap.get(xsdName));
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        assertEquals("Test of approved page", true, xmlValidatorModule.tryParsing(in, xsdUrl));
    }

    @org.junit.Test
    public void analyzeFailingXMLArticleTest() throws Exception {

        URL xmlurl = getClass().getClassLoader().getResource("testResources/articleFailTest.xml");
        ValidateXMLMain.ValidateXMLModule xmlValidatorModule = new ValidateXMLMain.ValidateXMLModule();
        String xsdName = xmlValidatorModule.getRootName( new InputSource(xmlurl.getFile()));
        Map<String, String> xsdMap = xmlValidatorModule.provideXsdRootMap();
        URL xsdUrl = getClass().getClassLoader().getResource(xsdMap.get(xsdName));
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        assertEquals("Test of failing article", false, xmlValidatorModule.tryParsing(in, xsdUrl));
    }


    @org.junit.Test
    public void analyzeFailingXMLPageTest() throws Exception {

        URL xmlurl = getClass().getClassLoader().getResource("testResources/pageFailTest.xml");
        ValidateXMLMain.ValidateXMLModule xmlValidatorModule = new ValidateXMLMain.ValidateXMLModule();
        String xsdName = xmlValidatorModule.getRootName( new InputSource(xmlurl.getFile()));
        Map<String, String> xsdMap = xmlValidatorModule.provideXsdRootMap();
        URL xsdUrl = getClass().getClassLoader().getResource(xsdMap.get(xsdName));
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        assertEquals("Test of failing page", false, xmlValidatorModule.tryParsing(in, xsdUrl));
    }

    @org.junit.Test
    public void invocationTest() throws URISyntaxException {

        ValidateXMLMain.main(new String[]{
                "xmlvalidate-vagrant.properties",
        });
    }
}
