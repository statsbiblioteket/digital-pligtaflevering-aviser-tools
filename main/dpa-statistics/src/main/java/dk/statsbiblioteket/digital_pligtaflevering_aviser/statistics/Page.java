package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Page in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
@XmlRootElement
public class Page implements java.io.Serializable {

    //The name of the article is really just the name of tne metadata file
    private String pageName;

    public Page() {
    }

    public Page(String pageName) {
        this.pageName = pageName;
    }

    @XmlAttribute
    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getPageName() {
        return pageName;
    }
}
