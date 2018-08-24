package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * DeliveryStatistics in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
@XmlRootElement
public class DeliveryStatistics implements java.io.Serializable {

    private String deliveryName = "";

    /**
     * List of all titles in the delivery meaning al newspapertitles
     */
    @XmlElement(name = "title", required = false)
    @XmlElementWrapper(name="titles")
    private List<Title> titles = new ArrayList<>();

    /**
     * Set the title of the setDeliveryName which is the original foldername "dl_########"
     */
    @XmlAttribute
    public void setDeliveryName(String deliveryName) {
        this.deliveryName = deliveryName;
    }

    /**
     * Set the title of the setDeliveryName which is the original foldername "dl_########"
     * @return deliveryName
     */
    public String getDeliveryName() {
        return this.deliveryName;
    }

    /**
     * Add a new Title to the delivery
     * @param title
     */
    public void addTitle(Title title) {
        titles.add(title);
    }

    /**
     * Get all titles that is contained in the delivery
     * @return
     */
    public List<Title> getTitles() {
        return titles;
    }
}
