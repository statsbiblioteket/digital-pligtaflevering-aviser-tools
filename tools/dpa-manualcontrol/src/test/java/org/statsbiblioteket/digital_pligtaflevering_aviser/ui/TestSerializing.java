package org.statsbiblioteket.digital_pligtaflevering_aviser.ui;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import org.junit.After;

import org.junit.Before;
import org.junit.Test;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.MissingItem;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.TitleDeliveryHierachy;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public void testMarshalUnmarshalDeliveryTitle() throws Exception {

        DeliveryTitleInfo deliveryTitleInfo = new DeliveryTitleInfo("dl_11111111", "test", 5, 7);

        File tempFile = new File("/tmp",  "MarshalUnmarshalDeliveryTitle.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(deliveryTitleInfo, tempFile);

        assertEquals(deliveryTitleInfo.getDeliveryName(), "dl_11111111");
        assertEquals(deliveryTitleInfo.getNewspaperTitle(), "test");
        assertEquals(deliveryTitleInfo.getNoOfArticles(), 5);
        assertEquals(deliveryTitleInfo.getNoOfPages(), 7);

        InputStream is = new FileInputStream("/tmp/MarshalUnmarshalDeliveryTitle.xml");

        JAXBContext jaxbContext1 = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
        DeliveryTitleInfo deserializedObject = (DeliveryTitleInfo)jaxbUnmarshaller.unmarshal(is);

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
        File tempFile = new File("/tmp",  "MissingItem.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(MissingItem.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(missingItem, tempFile);
        assertEquals(missingItem.getType(), "t1");
        assertEquals(missingItem.getValue(), "t2");

        InputStream is = new FileInputStream("/tmp/MissingItem.xml");

        JAXBContext jaxbContext1 = JAXBContext.newInstance(MissingItem.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
        MissingItem deserializedObject = (MissingItem)jaxbUnmarshaller.unmarshal(is);

        assertEquals(deserializedObject.getType(), "t1");
        assertEquals(deserializedObject.getValue(), "t2");

    }

    /**
     * Validate that TitleDeliveryHierachy handles deliverylists correctly, and that it can be streamed to and from xml
     * @throws Exception
     */
    @Test
    public void testMarshalUnmarshalTitleDeliveryHierachy() throws Exception {

        TitleDeliveryHierachy titleDeliveryHierachy = new TitleDeliveryHierachy();

        DeliveryTitleInfo ds = new DeliveryTitleInfo("dl_1234567_rt1", "JP", 1, 2);
        ds.addArticle(new Article("q"));
        ds.addArticle(new Article("a"));
        ds.addArticle(new Article("x"));
        ds.addPages(new Page("p1","p2"));
        ds.addPages(new Page("p3","p4"));
        ds.addPages(new Page("p5","p777"));

        MissingItem mis1  = new MissingItem("t11", "t21");
        MissingItem mis2  = new MissingItem("t12", "t22");
        MissingItem mis3  = new MissingItem("t13", "t23");

        ArrayList<MissingItem> list = new ArrayList<MissingItem>();
        list.add(mis1);
        list.add(mis2);
        list.add(mis3);

        ds.setMissingItems(list);


        titleDeliveryHierachy.addDeliveryToTitle(ds);
        titleDeliveryHierachy.addDeliveryToTitle(new DeliveryTitleInfo("dl_2234567_rt1", "JP", 3, 4));
        titleDeliveryHierachy.addDeliveryToTitle(new DeliveryTitleInfo("dl_3234567_rt1", "JP", 0, 0));
        titleDeliveryHierachy.addDeliveryToTitle(new DeliveryTitleInfo("dl_1234567_rt1", "JP", 0, 0));
        titleDeliveryHierachy.addDeliveryToTitle(new DeliveryTitleInfo("dl_2234567_rt1", "JP", 0, 0));
        titleDeliveryHierachy.addDeliveryToTitle(new DeliveryTitleInfo("dl_3234567_rt1", "BT", 0, 0));
        titleDeliveryHierachy.addDeliveryToTitle(new DeliveryTitleInfo("dl_3234567_rt2", "JP", 0, 0));

        assertEquals(2, titleDeliveryHierachy.getAllTitles().size());
        assertEquals(4, titleDeliveryHierachy.getDeliverysFromTitle("JP").size());
        assertEquals(1, titleDeliveryHierachy.getDeliverysFromTitle("BT").size());
        assertEquals(2, titleDeliveryHierachy.getDeliveryTitleObjects("dl_3234567_rt1").size());
        assertEquals(1, titleDeliveryHierachy.getDeliveryTitleObjects("dl_2234567_rt1").size());

        /*Wrapper wrapper = tabelsLayout.getDeliveries();*/
        File tempFile = new File("/tmp",  "TitleDeliveryHierachy.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(TitleDeliveryHierachy.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(titleDeliveryHierachy, tempFile);

        InputStream is = new FileInputStream("/tmp/TitleDeliveryHierachy.xml");

        JAXBContext jaxbContext1 = JAXBContext.newInstance(TitleDeliveryHierachy.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
        TitleDeliveryHierachy deserializedObject = (TitleDeliveryHierachy)jaxbUnmarshaller.unmarshal(is);

        assertEquals(2, deserializedObject.getAllTitles().size());
        assertEquals(4, deserializedObject.getDeliverysFromTitle("JP").size());
        assertEquals(1, deserializedObject.getDeliverysFromTitle("BT").size());
        assertEquals(2, deserializedObject.getDeliveryTitleObjects("dl_3234567_rt1").size());
        assertEquals(1, titleDeliveryHierachy.getDeliveryTitleObjects("dl_2234567_rt1").size());

    }


}
