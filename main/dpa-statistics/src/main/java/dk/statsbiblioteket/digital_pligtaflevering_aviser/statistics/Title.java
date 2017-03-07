package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Title in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
@XmlRootElement
public class Title implements java.io.Serializable {

    @XmlElement(name = "titleName")
    private String titleName;

    @XmlElement(name = "articles")
    private Articles articles = new Articles();

    @XmlElement(name = "pages")
    private Pages pages = new Pages();

    public Title() {
    }

    public Title(String titleName) {
        this.titleName = titleName;
    }


    public String getTitleName() {
        return titleName;
    }

    public void setTitle(String titleName) {
        this.titleName = titleName;
    }

    public void addArticle(Article name) {
        articles.addArticle(name);
    }

    public void setPages(List<Page> articles) {
        this.pages.setPages(articles);
    }

    public void addPage(Page name) {
        pages.addPage(name);
    }

    /*public List<Page> getPages() {
        return pages.getPages();
    }*/

    /*public List<Article> getArticles() {
        return articles.getArticles();
    }*/
}
