package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Articles;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Pages;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class DeliveryIdentifier implements java.io.Serializable {


    private String deliveryName;

    private String newspaperTitle;

    @XmlElement(name = "articles")
    private Articles articles = new Articles();


    private Pages pages = new Pages();

    @XmlAttribute(name = "noOfArticle")
    private int noOfArticles;

    @XmlAttribute(name = "noOfPage")
    private int noOfPages;

    private boolean checked = false;

    private String comment;

    private String initials;


    public DeliveryIdentifier() {
    }

    /**
     * construct the title with a titleName and an empty list of pages and articles
     * @param name
     */
    public DeliveryIdentifier(String deliveryName, String newspaperTitle, int noOfArticles, int noOfPages) {
        this.deliveryName = deliveryName;
        this.newspaperTitle = newspaperTitle;
        this.noOfArticles = noOfArticles;
        this.noOfPages = noOfPages;
    }

    @XmlAttribute
    public void setDeliveryName(String deliveryName) {
        this.deliveryName = deliveryName;
    }

    public String getDeliveryName() {
        return this.deliveryName;
    }

    @XmlAttribute
    public void setNewspaperTitle(String newspaperTitle) {
        this.newspaperTitle = newspaperTitle;
    }

    public String getNewspaperTitle() {
        return this.newspaperTitle;
    }


    public void addArticle(Article fileName) {
        articles.addArticle(fileName);
    }

    @XmlElement
    public void setPages(List<Page> pages) {
        this.pages.setPages(pages);
    }

    public List<Page> getPages() {
        return this.pages.getPages();
    }

    public void setArticles(List<Article> articles) {
        this.articles.setArticles(articles);
    }

    public void addPages(Page fileName) {
        pages.addPage(fileName);
    }


    public int getNoOfArticles() {
        return noOfArticles;
    }


    public int getNoOfPages() {
        return noOfPages;
    }


    public boolean isChecked() {
        return checked;
    }

    @XmlAttribute
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @XmlAttribute
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return this.comment;
    }

    @XmlAttribute
    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getInitials() {
        return this.initials;
    }
}
