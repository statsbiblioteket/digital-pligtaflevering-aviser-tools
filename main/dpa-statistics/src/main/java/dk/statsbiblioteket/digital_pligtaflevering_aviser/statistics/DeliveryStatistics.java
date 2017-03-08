package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DeliveryStatistics in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
@XmlRootElement
public class DeliveryStatistics implements java.io.Serializable {

    private String deliveryName = "";

    @XmlElement(name = "titles")
    private Titles titles = new Titles();


    @XmlElement
    public void setDeliveryName(String deliveryName) {
        this.deliveryName = deliveryName;
    }

    public String getDeliveryName() {
        return this.deliveryName;
    }

    public void addTitle(Title title) {
        titles.addTitle(title);
    }
}
