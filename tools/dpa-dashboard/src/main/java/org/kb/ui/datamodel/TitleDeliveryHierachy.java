package org.kb.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import dk.statsbiblioteket.medieplatform.autonomous.Delivery;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by mmj on 3/23/17.
 */
@XmlRootElement(name = "hierachy")
@XmlAccessorType(XmlAccessType.FIELD)
public class TitleDeliveryHierachy {

    @XmlElement(name = "titlexx")
    HashMap<String, DeliveryStuffs> deliveryStructure = new HashMap<String, DeliveryStuffs>();


    public void addDeliveryToTitle(String title, String delivery) {


        DeliveryStuffs deliveys;
        if(deliveryStructure.containsKey(title)) {
            deliveys = deliveryStructure.get(title);
        } else {
            deliveys = new DeliveryStuffs();
            deliveryStructure.put(title, deliveys);
        }


        deliveys.addTitle(new DeliveryStuff(delivery));
    }


}
