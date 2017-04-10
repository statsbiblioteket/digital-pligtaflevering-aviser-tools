package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

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
public class Titles
{
    @XmlElement(name = "title")
    private List<Title> titles = new ArrayList<Title>();

    /**
     * Get all Titles as a list
     * @return
     */
    public List<Title> getTitles() {
        return titles;
    }

    /**
     * Add a Title
     * @param title
     */
    public void addTitle(Title title) {
        this.titles.add(title);
    }

    /**
     * Set all Titles
     * @param titles
     */
    public void setTitles(List<Title> titles) {
        this.titles = titles;
    }
}