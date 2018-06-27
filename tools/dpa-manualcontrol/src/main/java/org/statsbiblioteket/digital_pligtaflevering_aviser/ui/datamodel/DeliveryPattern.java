package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;


import java.util.HashMap;
import java.util.LinkedHashMap;

public class DeliveryPattern {


    public static HashMap<String, LinkedHashMap<String, Boolean>> deliveryPatterns = new HashMap<String, LinkedHashMap<String, Boolean>>() {
        {
            put("aarhusstiftstidende",
                    new LinkedHashMap<String, Boolean>() {
                {
                    put("Mon",Boolean.TRUE);
                    put("Tue",Boolean.TRUE);
                    put("Wed",Boolean.TRUE);
                    put("Thu",Boolean.TRUE);
                    put("Fri",Boolean.TRUE);
                    put("Sat",Boolean.TRUE);
                    put("Sun",Boolean.TRUE);
                }
            });
            put("arbejderen",
                    new LinkedHashMap<String, Boolean>() {
                {
                    put("Mon",Boolean.FALSE);
                    put("Tue",Boolean.TRUE);
                    put("Wed",Boolean.TRUE);
                    put("Thu",Boolean.TRUE);
                    put("Fri",Boolean.TRUE);
                    put("Sat",Boolean.FALSE);
                    put("Sun",Boolean.FALSE);
                }
            });

            put("berlingsketidende",
                    new LinkedHashMap<String, Boolean>() {
                {
                    put("Mon",Boolean.TRUE);
                    put("Tue",Boolean.TRUE);
                    put("Wed",Boolean.TRUE);
                    put("Thu",Boolean.TRUE);
                    put("Fri",Boolean.TRUE);
                    put("Sat",Boolean.TRUE);
                    put("Sun",Boolean.TRUE);
                }
            });
        }
    };
}
