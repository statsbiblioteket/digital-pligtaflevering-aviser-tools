package org.kb.ui;

import org.junit.After;

import org.junit.Before;
import org.junit.Test;
import org.kb.ui.datamodel.UiDataConverter;


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


}
