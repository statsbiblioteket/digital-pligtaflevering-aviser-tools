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

    private String titleName;

    @XmlElement(name = "articles", required = false, namespace = "www.sb.dk/dpa/delivery")
    private Articles articles = new Articles();

    @XmlElement(name = "pages", required = false, namespace = "www.sb.dk/dpa/delivery")
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

    @XmlAttribute
    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getTitleName() {
        return this.titleName;
    }

    public int getNoOfArticles() {
        return this.articles.getArticles().size();
    }

    public int getNoOfPages() {
        return this.pages.getPages().size();
    }

    public List<Article> getArticle() {
        return this.articles.getArticles();
    }

    public List<Page> getPage() {
        return this.pages.getPages();
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
     * @param pages
     */
    public void setPages(List<Page> pages) {
        this.pages.setPages(pages);
    }

    /**
     * Add a new page
     * @param name
     */
    public void addPage(Page name) {
        pages.addPage(name);
    }
}
