package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Article in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
@XmlRootElement
public class Article implements java.io.Serializable {

    private String id;
    //The name of the article is really just the name of tne metadata file
    private String articleName;
    private String sectionName;
    private String sectionNumber;
    private String pageNumber;

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
}

