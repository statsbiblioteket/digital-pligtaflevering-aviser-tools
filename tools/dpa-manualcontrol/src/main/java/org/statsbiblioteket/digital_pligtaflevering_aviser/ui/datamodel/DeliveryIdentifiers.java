package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


/**
 * Titles in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
@XmlRootElement(name = "deliveries")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeliveryIdentifiers
{
    @XmlElement(name = "delivey")
    private List<DeliveryTitleInfo> deliveries = new ArrayList<DeliveryTitleInfo>();

    /**
     * Get all Titles as a list
     * @return
     */
    public List<DeliveryTitleInfo> getDeliveries() {
        return deliveries;
    }

    /**
     * Add a Title
     * @param delivery
     */
    public void addDeliverie(DeliveryTitleInfo delivery) {
        this.deliveries.add(delivery);
    }

    /**
     * Set all Titles
     * @param deliveries
     */
    public void setDeliveries(List<DeliveryTitleInfo> deliveries) {
        this.deliveries = deliveries;
    }
}