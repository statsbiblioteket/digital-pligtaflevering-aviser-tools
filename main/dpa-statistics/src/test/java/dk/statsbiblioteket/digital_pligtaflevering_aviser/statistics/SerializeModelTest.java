package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


/**
 * Unittest for validating the behavior of the parsing of results from VeraPDF
 */
public class SerializeModelTest {

    @Before
    public void setUp() throws Exception {
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

        File tempFile = createTestFile("testArticleSerialize");
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

        File tempFile = createTestFile("testPageSerialize");
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

        File tempFile = createTestFile("testTitleSerialize");

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

        XMLAssert.assertXpathExists("//title/*[local-name()='articles']/*[local-name()='article'][@articleName = 'title01']", xmlResult);
        XMLAssert.assertXpathExists("//title/*[local-name()='articles']/*[local-name()='article'][@articleName = 'title02']", xmlResult);
        XMLAssert.assertXpathExists("//title/*[local-name()='articles']/*[local-name()='article'][@articleName = 'title03']", xmlResult);

        XMLAssert.assertXpathExists("//title/*[local-name()='pages']/*[local-name()='page'][@pageName = 'title11']", xmlResult);
        XMLAssert.assertXpathExists("//title/*[local-name()='pages']/*[local-name()='page'][@pageName = 'title12']", xmlResult);
        XMLAssert.assertXpathExists("//title/*[local-name()='pages']/*[local-name()='page'][@pageName = 'title13']", xmlResult);

    }


    /**
     * Test of serializing of an entire DeliveryStatistics
     * The DeliveryStatistics is serialized into a xml-file and the content of the file is validated
     * @throws Exception
     */
    @Test
    public void testDeliverySerialize() throws Exception {

        File tempFile = createTestFile("testDeliverySerialize");

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

        jaxbMarshaller.marshal(deliveryStatistics, tempFile);
        String xmlResult = convertStreamToString(tempFile);

        XMLAssert.assertXpathEvaluatesTo("dl_213232", "//deliveryStatistics/@deliveryName", xmlResult);
        XMLAssert.assertXpathEvaluatesTo("test1", "//deliveryStatistics/*[local-name()='titles']/*[local-name()='title']/@titleName", xmlResult);
        XMLAssert.assertXpathEvaluatesTo("article1", "//deliveryStatistics/*[local-name()='titles']/*[local-name()='title']/*[local-name()='articles']/*[local-name()='article']/@articleName", xmlResult);
        XMLAssert.assertXpathEvaluatesTo("page1", "//deliveryStatistics/*[local-name()='titles']/*[local-name()='title']/*[local-name()='pages']/*[local-name()='page']/@pageName", xmlResult);

        XMLAssert.assertXpathExists("//deliveryStatistics/*[local-name()='titles']/*[local-name()='title'][@titleName = 'test1']", xmlResult);
        XMLAssert.assertXpathExists("//deliveryStatistics/*[local-name()='titles']/*[local-name()='title'][@titleName = 'test2']", xmlResult);


        XMLAssert.assertXpathExists("//deliveryStatistics/*[local-name()='titles']/*[local-name()='title']/*[local-name()='pages']/*[local-name()='page'][@pageName = 'page1']", xmlResult);
        XMLAssert.assertXpathExists("//deliveryStatistics/*[local-name()='titles']/*[local-name()='title']/*[local-name()='pages']/*[local-name()='page'][@pageName = 'page2']", xmlResult);

        XMLAssert.assertXpathExists("//deliveryStatistics/*[local-name()='titles']/*[local-name()='title']/*[local-name()='articles']/*[local-name()='article'][@articleName = 'article1']", xmlResult);
        XMLAssert.assertXpathExists("//deliveryStatistics/*[local-name()='titles']/*[local-name()='title']/*[local-name()='articles']/*[local-name()='article'][@articleName = 'article2']", xmlResult);
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