package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Title in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
@XmlRootElement
public class Title implements java.io.Serializable {

    @XmlAttribute(name = "titleName")
    private String titleName;

    @XmlElement(name = "articles")
    private Articles articles = new Articles();

    @XmlElement(name = "pages")
    private Pages pages = new Pages();

    public Title() {
    }

    /**
     * construct the title with a titleName and an empty list of pages and articles
     * @param titleName
     */
    public Title(String titleName) {
        this.titleName = titleName;
    }


    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    /**
     * add a new article to the list of articles
     * @param name
     */
    public void addArticle(Article name) {
        articles.addArticle(name);
    }

    /**
     * Set the entire list of pages
     * @param articles
     */
    public void setPages(List<Page> articles) {
        this.pages.setPages(articles);
    }

    /**
     * Add a new page
     * @param name
     */
    public void addPage(Page name) {
        pages.addPage(name);
    }
}
