package org.kb.ui.datamodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;

/**
 * Created by mmj on 3/23/17.
 */
@XmlRootElement(name = "hierachy")
@XmlAccessorType(XmlAccessType.FIELD)
public class TitleDeliveryHierachy {

    @XmlElement(name = "title")
    private HashMap<String, DeliveryIdentifiers> deliveryStructure = new HashMap<String, DeliveryIdentifiers>();


    public void addDeliveryToTitle(String title, DeliveryIdentifier ds) {

        DeliveryIdentifiers deliveys;
        if(deliveryStructure.containsKey(title)) {
            deliveys = deliveryStructure.get(title);
        } else {
            deliveys = new DeliveryIdentifiers();
            deliveryStructure.put(title, deliveys);
        }

        deliveys.addDeliverie(ds);
    }

    public DeliveryIdentifier setDeliveryTitleCheckStatus(String title, String delivery, boolean checked, String initials, String comment) {
        DeliveryIdentifiers jjj = deliveryStructure.get(title);
        for(DeliveryIdentifier delId : jjj.getDeliveries()) {
            if(delivery.equals(delId.getName())) {

                delId.setChecked(checked);
                delId.setInitials(initials);
                delId.setComment(comment);
                return delId;


            }
        }
        return null;
    }

    public HashMap<String, DeliveryIdentifiers> getDeliveryStructure() {
        return deliveryStructure;
    }

}
