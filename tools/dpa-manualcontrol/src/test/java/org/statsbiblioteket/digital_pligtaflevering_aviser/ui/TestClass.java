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
 * Created by mmj on 3/2/17.
 */
public class TestClass {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void testMarshalUnmarshalDeliveryTitle() throws Exception {

        DeliveryTitleInfo del = new DeliveryTitleInfo("test", "test", 5, 5);


        File tempFile = new File("/tmp",  "MarshalUnmarshalDeliveryTitle.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(del, tempFile);


        InputStream is = new FileInputStream("/tmp/MarshalUnmarshalDeliveryTitle.xml");

        JAXBContext jaxbContext1 = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
        DeliveryTitleInfo deserializedObject = (DeliveryTitleInfo)jaxbUnmarshaller.unmarshal(is);


        assertEquals(deserializedObject.getDeliveryName(), "test");

    }




    @Test
    public void testDateconversion() throws ParseException {


        String deliveryItemDirectoryName = "dl_20170224_rt1";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

        //Date date = UiDataConverter.getDateFromDeliveryItemDirectoryName(deliveryItemDirectoryName);

        Pattern pattern = Pattern.compile("dl_(.*)_rt([0-9]+)$");
        Matcher matcher = pattern.matcher(deliveryItemDirectoryName);
        if (matcher.matches()) {
            String datePart = matcher.group(1);
            String roundtripValue = matcher.group(2);

            Date d = formatter.parse(datePart);
            String weekday_name = new SimpleDateFormat("EEE", Locale.ENGLISH).format(d);




            System.out.println("TEST_DONE");
        }
    }



    @Test
    public void testMissingItemSerialize() throws Exception {

        MissingItem mis  = new MissingItem("t1", "t2");
        /*Wrapper wrapper = tabelsLayout.getDeliveries();*/
        File tempFile = new File("/tmp",  "MissingItem.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(MissingItem.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(mis, tempFile);

    }


    @Test
    public void testMarshalUnmarshalTitleDeliveryHierachy() throws Exception {


        TitleDeliveryHierachy t = new TitleDeliveryHierachy();

        DeliveryTitleInfo ds = new DeliveryTitleInfo("JP", "dl_1234567_rt1", 1, 2);
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


        t.addDeliveryToTitle(ds);
        t.addDeliveryToTitle(new DeliveryTitleInfo("JP", "dl_2234567_rt1", 3, 4));
        t.addDeliveryToTitle(new DeliveryTitleInfo("JP", "dl_3234567_rt1", 0, 0));
        t.addDeliveryToTitle(new DeliveryTitleInfo("JP", "dl_1234567_rt1", 0, 0));
        t.addDeliveryToTitle(new DeliveryTitleInfo("JP", "dl_2234567_rt1", 0, 0));
        t.addDeliveryToTitle(new DeliveryTitleInfo("JP", "dl_3234567_rt1", 0, 0));
        t.addDeliveryToTitle(new DeliveryTitleInfo("JP", "dl_3234567_rt2", 0, 0));



        /*Wrapper wrapper = tabelsLayout.getDeliveries();*/
        File tempFile = new File("/tmp",  "TitleDeliveryHierachy.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(TitleDeliveryHierachy.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(t, tempFile);

        System.out.println("TEST_DONE");

    }


}
