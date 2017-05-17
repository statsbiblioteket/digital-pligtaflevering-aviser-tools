package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Article in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
@XmlRootElement
public class Article implements java.io.Serializable {

    //The id of the article is the uuid which is used in fedora
    private String id;

    //The name of the article is really just the name of tne metadata file
    private String articleName;

    //Sectionname is the readable name of the section, it is the same as the one delivered in metada delivered by Infomadia
    private String sectionName;

    //The sectionNumber is the number of the section, it is the same as the one delivered in metada delivered by Infomadia
    private String sectionNumber;

    //The pageNumber is the number of the page, it is the same as the one delivered in metada delivered by Infomadia
    private String pageNumber;

    //checkedState indicates if a manual check of the delivered information has been performed
    private ConfirmationState checkedState = ConfirmationState.UNCHECKED;

    public Article() {
    }

    public Article(String articleName) {
        this.articleName = articleName;
    }

    public Article(String id, String articleName, String sectionName, String sectionNumber, String pageNumber) {
        this.id = id;
        this.articleName = articleName;
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
    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public String getArticleName() {
        return articleName;
    }

    @XmlAttribute
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }


    public String getSectionName() {
        return sectionName;
    }


    @XmlAttribute
    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    @XmlAttribute
    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    @XmlAttribute
    public void setCheckedState(ConfirmationState checkedState) {
        this.checkedState = checkedState;
    }

    public ConfirmationState getCheckedState() {
        return checkedState;
    }
}

