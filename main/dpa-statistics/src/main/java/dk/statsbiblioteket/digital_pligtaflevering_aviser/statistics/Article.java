package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Article in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
@XmlRootElement
public class Article implements java.io.Serializable {

    //The name of the article is really just the name of tne metadata file
    private String articleName;

    public Article() {
    }

    public Article(String articleName) {
        this.articleName = articleName;
    }

    @XmlAttribute
    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public String getArticleName() {
        return articleName;
    }
}

