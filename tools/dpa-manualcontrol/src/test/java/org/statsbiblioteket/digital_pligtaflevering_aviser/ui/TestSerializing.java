package org.statsbiblioteket.digital_pligtaflevering_aviser.ui;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;
import org.junit.After;

import org.junit.Before;
import org.junit.Test;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryPattern;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.MissingItem;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.TitleDeliveryHierarchy;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.WeekPattern;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


/**
 * Simple unittest for validation of the datamodel
 */
public class TestSerializing {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * Validate that it is possible to convert between xml and the object DeliveryTitleInfo
     * @throws Exception
     */
    @Test
    public void testMarshalUnmarshalDeliveryTitle2() throws Exception {

        DeliveryPattern deliveryTitleInfos = new DeliveryPattern();

        deliveryTitleInfos.addDeliveryPattern("aarhusstiftstidende", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("arbejderen", new WeekPattern(Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("berlingsketidende", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("boersen", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("bornholmstidende", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("bt", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("dagbladetkoege", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("dagbladetringsted", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("dagbladetroskilde", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("dagbladetstruer", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("dernordschleswiger", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("ekstrabladet", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("flensborgavis", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("fredericiadagblad1890", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("frederiksborgamtsavis", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("fyensstiftstidende", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("fynsamtsavissvendborg", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("helsingoerdagblad", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("herningfolkeblad", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("holstebrodagblad", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("horsensfolkeblad1866", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("information", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("jydskevestkystensoenderborg", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("jydskevestkystenbillund", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("jydskevestkystenvarde", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("jydskevestkystenesbjerg", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("jydskevestkystenhaderslev", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("jydskevestkystenkolding1995", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("jydskevestkystentoender", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("jydskevestkystenaabenraa", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("jydskevestkystenvejen", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("kristeligtdagblad", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("lemvigfolkeblad", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("licitationen", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("lollandfalstersfolketidende", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("metroxpressoest", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("metroxpressvest", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("midtjyllandsavis1857", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("morgenavisenjyllandsposten", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("morsoefolkeblad", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("nordjyskestiftstidendeaalborg", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("nordjyskestiftstidendehimmerland", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("nordjyskestiftstidendevendsyssel", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("nordvestnytholbaek", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("nordvestnytkalundborg", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("politiken", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("politikenweekly", new WeekPattern(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("randersamtsavis", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("ringkjoebingamtsdagblad", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("sjaellandskenaestved", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("sjaellandskeslagelse", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("skivefolkeblad", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("thisteddagblad", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));
        deliveryTitleInfos.addDeliveryPattern("vejleamtsfolkeblad", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("viborgstiftsfolkeblad", new WeekPattern(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE));
        deliveryTitleInfos.addDeliveryPattern("weekendavisen", new WeekPattern(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE));


        File tempFile = createTestFile("DeliveryPattern");
        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryPattern.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(deliveryTitleInfos, tempFile);


        InputStream is = new FileInputStream(tempFile);
        JAXBContext jaxbContext1 = JAXBContext.newInstance(DeliveryPattern.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
        DeliveryPattern deserializedObject = (DeliveryPattern) jaxbUnmarshaller.unmarshal(is);

        WeekPattern deliveryInfo = deserializedObject.getDeliveryPattern("viborgstiftsfolkeblad");
        assertEquals(deliveryInfo.getDayState("Mon"), Boolean.TRUE);
        assertEquals(deliveryInfo.getDayState("Tue"), Boolean.TRUE);
        assertEquals(deliveryInfo.getDayState("Wed"), Boolean.TRUE);
        assertEquals(deliveryInfo.getDayState("Thu"), Boolean.TRUE);
        assertEquals(deliveryInfo.getDayState("Fri"), Boolean.TRUE);
        assertEquals(deliveryInfo.getDayState("Sat"), Boolean.TRUE);
        assertEquals(deliveryInfo.getDayState("Sun"), Boolean.FALSE);


        Path xmlPath = MavenProjectsHelper.getRequiredPathTowardsRoot(NewspaperUI.class, "DeliveryPattern.xml");
        is = new FileInputStream(xmlPath.toFile());
        jaxbContext1 = JAXBContext.newInstance(DeliveryPattern.class);
        jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
        deserializedObject = (DeliveryPattern) jaxbUnmarshaller.unmarshal(is);

        deliveryInfo = deserializedObject.getDeliveryPattern("viborgstiftsfolkeblad");
        assertEquals(deliveryInfo.getDayState("Mon"), Boolean.TRUE);
        assertEquals(deliveryInfo.getDayState("Tue"), Boolean.TRUE);
        assertEquals(deliveryInfo.getDayState("Wed"), Boolean.TRUE);
        assertEquals(deliveryInfo.getDayState("Thu"), Boolean.TRUE);
        assertEquals(deliveryInfo.getDayState("Fri"), Boolean.TRUE);
        assertEquals(deliveryInfo.getDayState("Sat"), Boolean.TRUE);
        assertEquals(deliveryInfo.getDayState("Sun"), Boolean.FALSE);



    }



    /**
     * Validate that it is possible to convert between xml and the object DeliveryTitleInfo
     * @throws Exception
     */
    @Test
    public void testMarshalUnmarshalDeliveryTitle() throws Exception {

        DeliveryTitleInfo deliveryTitleInfo = new DeliveryTitleInfo("dl_11111111", "test", 5, 7);

        File tempFile = createTestFile("MarshalUnmarshalDeliveryTitle");
        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(deliveryTitleInfo, tempFile);

        assertEquals(deliveryTitleInfo.getDeliveryName(), "dl_11111111");
        assertEquals(deliveryTitleInfo.getNewspaperTitle(), "test");
        assertEquals(deliveryTitleInfo.getNoOfArticles(), 5);
        assertEquals(deliveryTitleInfo.getNoOfPages(), 7);

        InputStream is = new FileInputStream(tempFile);

        JAXBContext jaxbContext1 = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
        DeliveryTitleInfo deserializedObject = (DeliveryTitleInfo) jaxbUnmarshaller.unmarshal(is);

        assertEquals(deserializedObject.getDeliveryName(), "dl_11111111");
        assertEquals(deserializedObject.getNewspaperTitle(), "test");
        assertEquals(deserializedObject.getNoOfArticles(), 5);
        assertEquals(deserializedObject.getNoOfPages(), 7);
    }

    /**
     * Validate that it is possible to convert between xml and the object MissingItem
     * @throws Exception
     */
    @Test
    public void testMissingItemSerialize() throws Exception {

        MissingItem missingItem  = new MissingItem("t1", "t2");
        /*Wrapper wrapper = tabelsLayout.getDeliveries();*/
        File tempFile = createTestFile("MissingItem");
        JAXBContext jaxbContext = JAXBContext.newInstance(MissingItem.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(missingItem, tempFile);
        assertEquals(missingItem.getType(), "t1");
        assertEquals(missingItem.getValue(), "t2");

        InputStream is = new FileInputStream(tempFile);

        JAXBContext jaxbContext1 = JAXBContext.newInstance(MissingItem.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
        MissingItem deserializedObject = (MissingItem) jaxbUnmarshaller.unmarshal(is);

        assertEquals(deserializedObject.getType(), "t1");
        assertEquals(deserializedObject.getValue(), "t2");

    }

    /**
     * Validate that TitleDeliveryHierarchy handles deliverylists correctly, and that it can be streamed to and from xml
     * @throws Exception
     */
    @Test
    public void testMarshalUnmarshalTitleDeliveryHierachy() throws Exception {

        TitleDeliveryHierarchy titleDeliveryHierarchy = new TitleDeliveryHierarchy();

        DeliveryTitleInfo ds = new DeliveryTitleInfo("dl_1234567_rt1", "JP", 1, 2);
        ds.addArticle(new Article("q"));
        ds.addArticle(new Article("a"));
        ds.addArticle(new Article("x"));
        ds.addPages(new Page("p1", "p2"));
        ds.addPages(new Page("p3", "p4"));
        ds.addPages(new Page("p5", "p777"));

        MissingItem mis1  = new MissingItem("t11", "t21");
        MissingItem mis2  = new MissingItem("t12", "t22");
        MissingItem mis3  = new MissingItem("t13", "t23");

        ArrayList<MissingItem> list = new ArrayList<MissingItem>();
        list.add(mis1);
        list.add(mis2);
        list.add(mis3);

        ds.setMissingItems(list);


        titleDeliveryHierarchy.addDeliveryToTitle(ds);
        titleDeliveryHierarchy.addDeliveryToTitle(new DeliveryTitleInfo("dl_2234567_rt1", "JP", 3, 4));
        titleDeliveryHierarchy.addDeliveryToTitle(new DeliveryTitleInfo("dl_3234567_rt1", "JP", 0, 0));
        titleDeliveryHierarchy.addDeliveryToTitle(new DeliveryTitleInfo("dl_1234567_rt1", "JP", 0, 0));
        titleDeliveryHierarchy.addDeliveryToTitle(new DeliveryTitleInfo("dl_2234567_rt1", "JP", 0, 0));
        titleDeliveryHierarchy.addDeliveryToTitle(new DeliveryTitleInfo("dl_3234567_rt1", "BT", 0, 0));
        titleDeliveryHierarchy.addDeliveryToTitle(new DeliveryTitleInfo("dl_3234567_rt2", "JP", 0, 0));

        assertEquals(2, titleDeliveryHierarchy.getAllTitles().size());
        assertEquals(4, titleDeliveryHierarchy.getDeliverysFromTitle("JP").size());
        assertEquals(1, titleDeliveryHierarchy.getDeliverysFromTitle("BT").size());
        assertEquals(2, titleDeliveryHierarchy.getDeliveryTitleObjects("dl_3234567_rt1").size());
        assertEquals(1, titleDeliveryHierarchy.getDeliveryTitleObjects("dl_2234567_rt1").size());

        /*Wrapper wrapper = tabelsLayout.getDeliveries();*/
        File tempFile = createTestFile("TitleDeliveryHierarchy");
        JAXBContext jaxbContext = JAXBContext.newInstance(TitleDeliveryHierarchy.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(titleDeliveryHierarchy, tempFile);

        InputStream is = new FileInputStream(tempFile);

        JAXBContext jaxbContext1 = JAXBContext.newInstance(TitleDeliveryHierarchy.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
        TitleDeliveryHierarchy deserializedObject = (TitleDeliveryHierarchy) jaxbUnmarshaller.unmarshal(is);

        assertEquals(2, deserializedObject.getAllTitles().size());
        assertEquals(4, deserializedObject.getDeliverysFromTitle("JP").size());
        assertEquals(1, deserializedObject.getDeliverysFromTitle("BT").size());
        assertEquals(2, deserializedObject.getDeliveryTitleObjects("dl_3234567_rt1").size());
        assertEquals(1, titleDeliveryHierarchy.getDeliveryTitleObjects("dl_2234567_rt1").size());

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
