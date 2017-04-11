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
    private String sectionNumber;
    private String pageNumber;
    private ConfirmationState checkedState = ConfirmationState.UNCHECKED;

    public Page() {
    }

    public Page(String pageName, String sectionName) {
        this.pageName = pageName;
        this.sectionName = sectionName;
    }

    public Page(String id, String pageName, String sectionName, String sectionNumber, String pageNumber) {
        this.id = id;
        this.pageName = pageName;
        this.sectionName = sectionName;
        this.sectionNumber = sectionNumber;
        this.pageNumber = pageNumber;
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


    public String getSectionNumber() {
        return sectionNumber;
    }

    @XmlAttribute
    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    @XmlAttribute
    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    public ConfirmationState getCheckedState() {
        return checkedState;
    }

    @XmlAttribute
    public void setCheckedState(ConfirmationState checkedState) {
        this.checkedState = checkedState;
    }
}
