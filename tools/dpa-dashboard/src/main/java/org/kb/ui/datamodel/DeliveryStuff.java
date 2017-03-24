package org.kb.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Articles;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Pages;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class DeliveryStuff implements java.io.Serializable {


    @XmlAttribute(name = "titleName")
    private String titleName;


    public DeliveryStuff() {
    }

    /**
     * construct the title with a titleName and an empty list of pages and articles
     * @param titleName
     */
    public DeliveryStuff(String titleName) {
        this.titleName = titleName;
    }


    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getTitle() {
        return this.titleName;
    }

}
