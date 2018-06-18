package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Page in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
@XmlRootElement
public class Page implements java.io.Serializable {

    private String id;
    //The name of the article is really just the name of tne metadata file
    private String pageName;
    private String sectionName;
    private String section;
    private String page;
    private ConfirmationState chk = ConfirmationState.UNCHECKED;

    public Page() {
    }

    public Page(String pageName, String sectionName) {
        this.pageName = pageName;
        this.sectionName = sectionName;
    }

    public Page(String id, String pageName, String sectionName, String section, String page) {
        this.id = id;
        this.pageName = pageName;
        this.sectionName = sectionName;
        this.section = section;
        this.page = page;
    }

    @XmlAttribute
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @XmlAttribute
    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getPageName() {
        return pageName;
    }

    public String getSectionName() {
        return sectionName;
    }

    @XmlAttribute
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }


    public String getSection() {
        return section;
    }

    @XmlAttribute(name = "sectionNumber")
    public void setSection(String section) {
        this.section = section;
    }

    public String getPage() {
        return page;
    }

    @XmlAttribute(name = "pageNumber")
    public void setPage(String page) {
        this.page = page;
    }

    public ConfirmationState getChk() {
        return chk;
    }

    @XmlAttribute(name="checkedState")
    public void setChk(ConfirmationState chk) {
        this.chk = chk;
    }
}
