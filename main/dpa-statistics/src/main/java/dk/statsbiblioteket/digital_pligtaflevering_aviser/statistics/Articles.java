package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Articles in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
@XmlRootElement(name = "articles")
@XmlAccessorType (XmlAccessType.FIELD)
public class Articles
{
    @XmlElement(name = "article")
    private List<Article> articles = new ArrayList<Article>();

    public List<Article> getArticles() {
        return articles;
    }

    public void addArticle(Article article) {
        this.articles.add(article);
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }
}