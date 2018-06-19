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

    //The section is the number of the section, it is the same as the one delivered in metada delivered by Infomadia
    private String section;

    //The page is the number of the page, it is the same as the one delivered in metada delivered by Infomadia
    private String page;

    //chk indicates if a manual check of the delivered information has been performed
    private ConfirmationState chk = ConfirmationState.UNCHECKED;

    public Article() {
    }

    public Article(String articleName) {
        this.articleName = articleName;
    }

    public Article(String id, String articleName, String sectionName, String section, String page) {
        this.id = id;
        this.articleName = articleName;
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


    @XmlAttribute(name = "sectionNumber")
    public void setSection(String section) {
        this.section = section;
    }

    public String getSection() {
        return section;
    }

    @XmlAttribute(name = "pageNumber")
    public void setPage(String page) {
        this.page = page;
    }

    public String getPage() {
        return page;
    }

    @XmlAttribute(name = "checkedState")
    public void setChk(ConfirmationState chk) {
        this.chk = chk;
    }

    public ConfirmationState getChk() {
        return chk;
    }
}

