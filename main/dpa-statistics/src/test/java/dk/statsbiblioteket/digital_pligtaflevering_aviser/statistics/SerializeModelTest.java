package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
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
     * Test against an output with 6 broken rules, one of them is UNKNOWN
     * @throws Exception
     */
    @Test
    public void testArticleSerialize() throws Exception {

        File tempFile = createTestFile("/tmp/testTitleSerialize.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(Article.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        Article article = new Article("ArticleTestName");
        jaxbMarshaller.marshal(article, tempFile);
        String xmlResult = convertStreamToString(tempFile);
        XMLAssert.assertXpathEvaluatesTo("ArticleTestName", "//articleName", xmlResult);
    }

    /**
     * Test of an output with no broken rules
     * @throws Exception
     */
    @Test
    public void testPageSerialize() throws Exception {

        File tempFile = createTestFile("/tmp/testTitleSerialize.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(Page.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        Page article = new Page("PageTestName");
        jaxbMarshaller.marshal(article, tempFile);
        String xmlResult = convertStreamToString(tempFile);
        XMLAssert.assertXpathEvaluatesTo("PageTestName", "//pageName", xmlResult);
    }


    /**
     * Test of an output with no broken rules
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

        title.addPage(new Page("title11"));
        title.addPage(new Page("title12"));
        title.addPage(new Page("title13"));

        Title title2 = new Title("TitleName2");
        title2.addArticle(new Article("title01"));
        title2.addArticle(new Article("title02"));
        title2.addArticle(new Article("title03"));

        title2.addPage(new Page("title11"));
        title2.addPage(new Page("title12"));
        title2.addPage(new Page("title13"));

        QName qName = new QName("com.codenotfound.jaxb.model", "test");
        JAXBElement<Title> root = new JAXBElement<Title>(qName, Title.class, title);

        jaxbMarshaller.marshal(root, tempFile);
        String xmlResult = convertStreamToString(tempFile);
        XMLAssert.assertXpathEvaluatesTo("TitleName1", "//titleName", xmlResult);
    }


    /**
     * Test of serializing of an entire Delivery
     * The Delivery is serialized into a xml-file and the content of the file is validated
     * @throws Exception
     */
    @Test
    public void testDeliverySerialize() throws Exception {

        File tempFile = createTestFile("/tmp/testDeliverySerialize.xml");

        JAXBContext jaxbContext = JAXBContext.newInstance(Delivery.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);

        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        Delivery delivery = new Delivery();
        delivery.setDeliveryName("dl_213232");
        Title titleAdd1 = new Title("test1");
        delivery.addTitle(titleAdd1);
        titleAdd1.addArticle(new Article("article1"));
        titleAdd1.addArticle(new Article("article2"));

        titleAdd1.addPage(new Page("page1"));
        titleAdd1.addPage(new Page("page2"));
        titleAdd1.addPage(new Page("page3"));


        Title titleAdd2 = new Title("test2");
        delivery.addTitle(titleAdd2);
        titleAdd2.addArticle(new Article("articlea1"));
        titleAdd2.addArticle(new Article("articlea2"));
        titleAdd2.addArticle(new Article("articlea2"));
        titleAdd2.addArticle(new Article("articlea2"));
        titleAdd2.addArticle(new Article("articlea2"));

        titleAdd2.addPage(new Page("page1"));
        titleAdd2.addPage(new Page("page2"));

        jaxbMarshaller.marshal(delivery, tempFile);
        String xmlResult = convertStreamToString(tempFile);

        XMLAssert.assertXpathEvaluatesTo("dl_213232", "//delivery/deliveryName", xmlResult);
        XMLAssert.assertXpathEvaluatesTo("test1", "//delivery/titles/title/titleName", xmlResult);
        XMLAssert.assertXpathEvaluatesTo("article1", "//delivery/titles/title/articles/article/articleName", xmlResult);
        XMLAssert.assertXpathEvaluatesTo("page1", "//delivery/titles/title/pages/page/pageName", xmlResult);
    }


    /**
     * Test of serializing of an entire Delivery
     * The Delivery is serialized into a xml-file and the content of the file is validated
     * @throws Exception
     */
    @Test
    public void testDeliveryUnserialize() throws Exception {

        File tempFile = new File("/tmp/testDeliverySerialize.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(Delivery.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Delivery deserializedObject = (Delivery)jaxbUnmarshaller.unmarshal(tempFile);
        assertEquals(deserializedObject.getDeliveryName(), "dl_213232");
    }

    /**
     * Create a temporary testFile
     * @param filename
     * @return
     * @throws IOException
     */
    private File createTestFile(String filename) throws IOException {
        File f = Paths.get(filename).toFile();
        f.deleteOnExit();
        return f;
    }

    /**
     * Read the content of a file and return it as a UTF-8 encoded String
     * @param file
     * @return
     * @throws Exception
     */
    static String convertStreamToString(java.io.File file) throws Exception {
        Scanner scanner = new Scanner( file, "UTF-8" );
        String xmlResult = scanner.useDelimiter("\\A").next();
        scanner.close(); // Put this call in a finally block
        return xmlResult;
    }
}
