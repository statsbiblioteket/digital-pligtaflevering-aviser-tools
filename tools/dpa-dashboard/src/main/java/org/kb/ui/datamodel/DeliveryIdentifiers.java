package org.kb.ui.datamodel;

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
    private List<DeliveryIdentifier> deliveries = new ArrayList<DeliveryIdentifier>();

    /**
     * Get all Titles as a list
     * @return
     */
    public List<DeliveryIdentifier> getDeliveries() {
        return deliveries;
    }

    /**
     * Add a Title
     * @param delivery
     */
    public void addDeliverie(DeliveryIdentifier delivery) {
        this.deliveries.add(delivery);
    }

    /**
     * Set all Titles
     * @param deliveries
     */
    public void setDeliveries(List<DeliveryIdentifier> deliveries) {
        this.deliveries = deliveries;
    }
}