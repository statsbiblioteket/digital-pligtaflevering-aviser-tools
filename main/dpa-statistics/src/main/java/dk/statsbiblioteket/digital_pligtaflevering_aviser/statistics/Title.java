package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Title in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
public class Title implements java.io.Serializable {

    private String titleName;

    @XmlElement(name = "article", required = false)
    @XmlElementWrapper(name="articles")
    private List<Article> articles = new ArrayList<>();

    @XmlElement(name = "page", required = false)
    @XmlElementWrapper(name="pages")
    private List<Page> pages = new ArrayList<>();

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
        return this.articles.size();
    }

    public int getNoOfPages() {
        return this.pages.size();
    }

    public List<Article> getArticle() {
        return this.articles;
    }

    public List<Page> getPage() {
        return this.pages;
    }

    public List<Page> getFrontpages() {
        return pages.stream().filter(f -> f.getPageNumber().equals("1")).collect(Collectors.toList());
    }


    /**
     * add a new article to the list of articles
     * @param name
     */
    public void addArticle(Article name) {
        articles.add(name);
    }

    /**
     * Set the entire list of pages
     * @param pages
     */
    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    /**
     * Add a new page
     * @param name
     */
    public void addPage(Page name) {
        pages.add(name);
    }
}
