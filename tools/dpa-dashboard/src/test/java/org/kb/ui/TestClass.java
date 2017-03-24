package org.kb.ui;

import org.junit.After;

import org.junit.Before;
import org.junit.Test;
import org.kb.ui.datamodel.DeliveryStuff;
import org.kb.ui.datamodel.TitleDeliveryHierachy;
import org.kb.ui.datamodel.UiDataConverter;


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
        t.addDeliveryToTitle("JP", "dl_1234567_rt1");
        t.addDeliveryToTitle("JP", "dl_2234567_rt1");
        t.addDeliveryToTitle("JP", "dl_3234567_rt1");
        t.addDeliveryToTitle("bt", "dl_1234567_rt1");
        t.addDeliveryToTitle("bt", "dl_2234567_rt1");
        t.addDeliveryToTitle("bt", "dl_3234567_rt1");
        t.addDeliveryToTitle("JP", "dl_3234567_rt2");



        /*Wrapper wrapper = tabelsLayout.getTitles();*/
        File tempFile = new File("/home/mmj/tools/tomcat",  "test.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(TitleDeliveryHierachy.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(t, tempFile);

        System.out.println("TEST_DONE");

    }


}
