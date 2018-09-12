package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import dk.statsbiblioteket.util.Pair;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


/**
 * Unittest for validating the behavior of the parsing of results from VeraPDF
 */
public class SerializeModelTest {
    
    public static final XPathSelector XPATH = DOM.createXPathSelector("ns", "kb.dk/dpa/delivery-statistics");
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }



    /**
     * Test of serializing of an entire DeliveryStatistics
     * The DeliveryStatistics is serialized into a xml-file and the content of the file is validated
     * @throws Exception
     */
    @Test
    public void testDeliverySerialize() throws Exception {

        DeliveryStatistics deliveryStatistics = new DeliveryStatistics();
        deliveryStatistics.setDeliveryName("dl_213232");
        Title titleAdd1 = new Title("test1");
        deliveryStatistics.addTitle(titleAdd1);
        titleAdd1.addArticle(new Article("article1"));
        titleAdd1.addArticle(new Article("article2"));

        titleAdd1.addPage(new Page("uuid1", "page1", "section", "pname", "pno"));
        titleAdd1.addPage(new Page("uuid2", "page2", "section", "pname", "pno"));
        titleAdd1.addPage(new Page("uuid3", "page3", "section", "pname", "pno"));


        Title titleAdd2 = new Title("test2");
        deliveryStatistics.addTitle(titleAdd2);
        titleAdd2.addArticle(new Article("article1"));
        titleAdd2.addArticle(new Article("article2"));
        titleAdd2.addArticle(new Article("article2"));
        titleAdd2.addArticle(new Article("article2"));
        titleAdd2.addArticle(new Article("article2"));

        titleAdd2.addPage(new Page("uuid4", "page1", "section", "pname", "pno"));
        titleAdd2.addPage(new Page("uuid5", "page2", "section", "pname", "pno"));

        //Hack to return two values without to much fuss
        Pair<String, Document> result = objectToXml(deliveryStatistics);
        String xmlResult = result.getLeft();
        Document doc = result.getRight();


        /*Assert.assertEquals(xmlResult,"dl_213232",
                XPATH.selectString(doc,
                        "/ns:deliveryStatistics/@deliveryName"));*/

        NodeList nl = XPATH.selectNodeList(doc,
                "/ns:deliveryStatistics/ns:titles/ns:title[count(ns:pages/ns:page) > 0]/@titleName");

        Assert.assertEquals(nl.getLength(), 2);


        Assert.assertEquals(xmlResult,"dl_213232",
                            XPATH.selectString(doc,
                                               "/ns:deliveryStatistics/@deliveryName"));
    
        Assert.assertEquals(xmlResult,"test1",
                            XPATH.selectString(doc,
                                               "/ns:deliveryStatistics/ns:titles/ns:title/@titleName"));

        Assert.assertEquals(xmlResult,"article1",
                    XPATH.selectString(doc,
                                               "/ns:deliveryStatistics/ns:titles/ns:title/ns:articles/ns:article/@articleName"));
    
        Assert.assertEquals(xmlResult,"page1",
                            XPATH.selectString(doc,
                                               "/ns:deliveryStatistics/ns:titles/ns:title/ns:pages/ns:page/@pageName"));

        
        Assert.assertNotNull(xmlResult,
                             XPATH.selectNode(doc,
                                              "/ns:deliveryStatistics/ns:titles/ns:title[@titleName = 'test1']"));
        Assert.assertNotNull(xmlResult,
                             XPATH.selectNode(doc,
                                              "/ns:deliveryStatistics/ns:titles/ns:title[@titleName = 'test2']"));
    
        Assert.assertNotNull(xmlResult,
                             XPATH.selectNode(doc,
                                              "/ns:deliveryStatistics/ns:titles/ns:title/ns:pages/ns:page[@pageName = 'page1']"));
        Assert.assertNotNull(xmlResult,
                             XPATH.selectNode(doc,
                                              "/ns:deliveryStatistics/ns:titles/ns:title/ns:pages/ns:page[@pageName = 'page2']"));
    
        Assert.assertNotNull(xmlResult,
                             XPATH.selectNode(doc,
                                              "/ns:deliveryStatistics/ns:titles/ns:title/ns:articles/ns:article[@articleName = 'article1']"));
        Assert.assertNotNull(xmlResult,
                             XPATH.selectNode(doc,
                                              "/ns:deliveryStatistics/ns:titles/ns:title/ns:articles/ns:article[@articleName = 'article2']"));
    }
    
    private <T> Pair<String, Document> objectToXml(T object)
            throws IOException, JAXBException, TransformerException {
        Pair<String,Document> result;
        try (StringWriter writer = new StringWriter();) {
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    
            jaxbMarshaller.marshal(object, writer);
            Document doc = DOM.stringToDOM(writer.toString(), true);
            String xmlResult = DOM.domToString(doc);
            result = new Pair<>(xmlResult,doc);
        }
        return result;
    }


    /**
     * Create a temporary testFile
     * @param filename
     * @return
     * @throws IOException
     */
    private File createTestFile(String filename) throws IOException {
        File tempFile = File.createTempFile(filename, ".xml");
        return tempFile;
    }

}
