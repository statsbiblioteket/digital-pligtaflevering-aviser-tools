package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Pages in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
@XmlRootElement(name = "pages")
@XmlAccessorType(XmlAccessType.FIELD)
public class Pages
{
    @XmlElement(name = "page")
    private List<Page> pages = new ArrayList<Page>();

    public void addPage(Page article) {
        this.pages.add(article);
    }

    public void setPages(List<Page> articles) {
        this.pages = articles;
    }
}