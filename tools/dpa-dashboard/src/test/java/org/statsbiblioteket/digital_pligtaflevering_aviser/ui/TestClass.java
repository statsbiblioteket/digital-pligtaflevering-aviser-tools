package org.statsbiblioteket.digital_pligtaflevering_aviser.ui;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import org.junit.After;

import org.junit.Before;
import org.junit.Test;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.TitleDeliveryHierachy;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryIdentifier;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



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
    public void testIng() throws ParseException {

        String deliveryItemDirectoryName = "dl_20170224_rt1";

        Date date = UiDataConverter.getDateFromDeliveryItemDirectoryName(deliveryItemDirectoryName);

        Pattern pattern = Pattern.compile("dl_(.*)_rt([0-9]+)$");
        Matcher matcher = pattern.matcher(deliveryItemDirectoryName);
        if (matcher.matches()) {
            String datePart = matcher.group(1);
            String roundtripValue = matcher.group(2);


            System.out.println("TEST_DONE");
        }
    }


    @Test
    public void testIng2() throws Exception {


        TitleDeliveryHierachy t = new TitleDeliveryHierachy();

        DeliveryIdentifier ds = new DeliveryIdentifier("dl_1234567_rt1", 1, 2);
        ds.addArticle(new Article("q"));
        ds.addArticle(new Article("a"));
        ds.addArticle(new Article("x"));
        ds.addPages(new Page("p1","p2"));
        ds.addPages(new Page("p3","p4"));
        ds.addPages(new Page("p5","p777"));


        t.addDeliveryToTitle("JP", ds);
        t.addDeliveryToTitle("JP", new DeliveryIdentifier("dl_2234567_rt1", 3, 4));
        t.addDeliveryToTitle("JP", new DeliveryIdentifier("dl_3234567_rt1", 0, 0));
        t.addDeliveryToTitle("bt", new DeliveryIdentifier("dl_1234567_rt1", 0, 0));
        t.addDeliveryToTitle("bt", new DeliveryIdentifier("dl_2234567_rt1", 0, 0));
        t.addDeliveryToTitle("bt", new DeliveryIdentifier("dl_3234567_rt1", 0, 0));
        t.addDeliveryToTitle("JP", new DeliveryIdentifier("dl_3234567_rt2", 0, 0));



        /*Wrapper wrapper = tabelsLayout.getDeliveries();*/
        File tempFile = new File("/home/mmj/tools/tomcat",  "test.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(TitleDeliveryHierachy.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(t, tempFile);

        System.out.println("TEST_DONE");

    }


}
