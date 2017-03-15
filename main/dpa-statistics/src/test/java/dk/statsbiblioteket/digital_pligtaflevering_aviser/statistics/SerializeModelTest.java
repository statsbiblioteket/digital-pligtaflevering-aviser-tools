package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;


/**
 * Unittest for validating the behavior of the parsing of results from VeraPDF
 */
public class SerializeModelTest {

    private BufferedReader reader;
    private BufferedOutputStream out;
    private PipedInputStream pipeInput;

    @Before
    public void setUp() throws Exception {
        pipeInput = new PipedInputStream();
        reader = new BufferedReader(new InputStreamReader(pipeInput));
        out = new BufferedOutputStream(new PipedOutputStream(pipeInput));
    }

    @After
    public void tearDown() throws Exception {

    }


    /**
     * Test that Article can get serialized into an xml-file, and that the xml-file contains the name of the article
     * @throws Exception
     */
    @Test
    public void testArticleSerialize() throws Exception {

        File tempFile = createTestFile("/tmp/testArticleSerialize.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(Article.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        Article article = new Article("ArticleTestName");
        jaxbMarshaller.marshal(article, tempFile);
        String xmlResult = convertStreamToString(tempFile);
        XMLAssert.assertXpathEvaluatesTo("ArticleTestName", "//@articleName", xmlResult);
    }

    /**
     * Page that Article can get serialized into an xml-file, and that the xml-file contains the name of the article
     * @throws Exception
     */
    @Test
    public void testPageSerialize() throws Exception {

        File tempFile = createTestFile("/tmp/testPageSerialize.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(Page.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        Page article = new Page("PageTestName", "section");
        jaxbMarshaller.marshal(article, tempFile);
        String xmlResult = convertStreamToString(tempFile);
        XMLAssert.assertXpathEvaluatesTo("PageTestName", "//@pageName", xmlResult);
    }


    /**
     * Page that Title can get serialized into an xml-file, and that the xml-file contains the name of the article
     * @throws Exception
     */
    @Test
    public void testTitleSerialize() throws Exception {

        File tempFile = createTestFile("/tmp/testTitleSerialize.xml");

        JAXBContext jaxbContext = JAXBContext.newInstance(Title.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        Title title = new Title("TitleName1");
        title.addArticle(new Article("title01"));
        title.addArticle(new Article("title02"));
        title.addArticle(new Article("title03"));

        title.addPage(new Page("title11", "section"));
        title.addPage(new Page("title12", "section"));
        title.addPage(new Page("title13", "section"));

        jaxbMarshaller.marshal(title, tempFile);
        String xmlResult = convertStreamToString(tempFile);
        XMLAssert.assertXpathEvaluatesTo("TitleName1", "//title/@titleName", xmlResult);
        XMLAssert.assertXpathEvaluatesTo("title01", "//title/articles/article/@articleName", xmlResult);

        XMLAssert.assertXpathExists("//title/articles/article[@articleName = 'title01']", xmlResult);
        XMLAssert.assertXpathExists("//title/articles/article[@articleName = 'title02']", xmlResult);
        XMLAssert.assertXpathExists("//title/articles/article[@articleName = 'title03']", xmlResult);

        XMLAssert.assertXpathExists("//title/pages/page[@pageName = 'title11']", xmlResult);
        XMLAssert.assertXpathExists("//title/pages/page[@pageName = 'title12']", xmlResult);
        XMLAssert.assertXpathExists("//title/pages/page[@pageName = 'title13']", xmlResult);

    }


    /**
     * Test of serializing of an entire DeliveryStatistics
     * The DeliveryStatistics is serialized into a xml-file and the content of the file is validated
     * @throws Exception
     */
    @Test
    public void testDeliverySerialize() throws Exception {

        File tempFile = createTestFile("/tmp/testDeliverySerialize.xml");

        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryStatistics.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);

        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        DeliveryStatistics deliveryStatistics = new DeliveryStatistics();
        deliveryStatistics.setDeliveryName("dl_213232");
        Title titleAdd1 = new Title("test1");
        deliveryStatistics.addTitle(titleAdd1);
        titleAdd1.addArticle(new Article("article1"));
        titleAdd1.addArticle(new Article("article2"));

        titleAdd1.addPage(new Page("page1", "section"));
        titleAdd1.addPage(new Page("page2", "section"));
        titleAdd1.addPage(new Page("page3", "section"));


        Title titleAdd2 = new Title("test2");
        deliveryStatistics.addTitle(titleAdd2);
        titleAdd2.addArticle(new Article("article1"));
        titleAdd2.addArticle(new Article("article2"));
        titleAdd2.addArticle(new Article("article2"));
        titleAdd2.addArticle(new Article("article2"));
        titleAdd2.addArticle(new Article("article2"));

        titleAdd2.addPage(new Page("page1", "section"));
        titleAdd2.addPage(new Page("page2", "section"));

        jaxbMarshaller.marshal(deliveryStatistics, tempFile);
        String xmlResult = convertStreamToString(tempFile);

        XMLAssert.assertXpathEvaluatesTo("dl_213232", "//deliveryStatistics/@deliveryName", xmlResult);
        XMLAssert.assertXpathEvaluatesTo("test1", "//deliveryStatistics/titles/title/@titleName", xmlResult);
        XMLAssert.assertXpathEvaluatesTo("article1", "//deliveryStatistics/titles/title/articles/article/@articleName", xmlResult);
        XMLAssert.assertXpathEvaluatesTo("page1", "//deliveryStatistics/titles/title/pages/page/@pageName", xmlResult);

        XMLAssert.assertXpathExists("//deliveryStatistics/titles/title[@titleName = 'test1']", xmlResult);
        XMLAssert.assertXpathExists("//deliveryStatistics/titles/title[@titleName = 'test2']", xmlResult);


        XMLAssert.assertXpathExists("//deliveryStatistics/titles/title/pages/page[@pageName = 'page1']", xmlResult);
        XMLAssert.assertXpathExists("//deliveryStatistics/titles/title/pages/page[@pageName = 'page2']", xmlResult);

        XMLAssert.assertXpathExists("//deliveryStatistics/titles/title/articles/article[@articleName = 'article1']", xmlResult);
        XMLAssert.assertXpathExists("//deliveryStatistics/titles/title/articles/article[@articleName = 'article2']", xmlResult);
    }


    /**
     * Test of serializing of an entire DeliveryStatistics
     * The DeliveryStatistics is serialized into a xml-file and the content of the file is validated
     * @throws Exception
     */
    @Test
    public void testDeliveryUnserialize() throws Exception {

        File tempFile = new File("/tmp/testDeliverySerialize.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryStatistics.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        DeliveryStatistics deserializedObject = (DeliveryStatistics)jaxbUnmarshaller.unmarshal(tempFile);
        assertEquals(deserializedObject.getDeliveryName(), "dl_213232");

        assertEquals(deserializedObject.getTitles().getTitles().size(), 2);
    }

    /**
     * Create a temporary testFile
     * @param filename
     * @return
     * @throws IOException
     */
    private File createTestFile(String filename) throws IOException {
        File f = Paths.get(filename).toFile();
        //f.deleteOnExit();
        return f;
    }

    /**
     * Read the content of a file and return it as a UTF-8 encoded String
     * @param file
     * @return
     * @throws Exception
     */
    static String convertStreamToString(java.io.File file) throws Exception {
        Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name());
        String xmlResult = scanner.useDelimiter("\\A").next();
        scanner.close(); // Put this call in a finally block
        return xmlResult;
    }
}
