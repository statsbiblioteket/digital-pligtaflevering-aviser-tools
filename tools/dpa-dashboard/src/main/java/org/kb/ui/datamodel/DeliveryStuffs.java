package org.kb.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


/**
 * Titles in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
@XmlRootElement(name = "titles")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeliveryStuffs
{
    @XmlElement(name = "title")
    private List<DeliveryStuff> titles = new ArrayList<DeliveryStuff>();

    /**
     * Get all Titles as a list
     * @return
     */
    public List<DeliveryStuff> getTitles() {
        return titles;
    }

    /**
     * Add a Title
     * @param title
     */
    public void addTitle(DeliveryStuff title) {
        this.titles.add(title);
    }

    /**
     * Set all Titles
     * @param titles
     */
    public void setTitles(List<DeliveryStuff> titles) {
        this.titles = titles;
    }
}