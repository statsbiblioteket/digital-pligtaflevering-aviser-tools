package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * DeliveryTitleInfo contains the validation-content in a delivery and a title
 */
@XmlRootElement
public class DeliveryTitleInfo implements java.io.Serializable {

    private String deliveryName;
    private String newspaperTitle;
    private List<Article> articles = new ArrayList<Article>();
    private List<Page> pages = new ArrayList<Page>();
    @XmlAttribute(name = "noOfArticle")
    private int noOfArticles;
    @XmlAttribute(name = "noOfPage")
    private int noOfPages;
    private boolean checked = false;
    private String comment;
    private String initials;
    private List<MissingItem> missingItems = new ArrayList<MissingItem>();


    public DeliveryTitleInfo() {
    }

    /**
     * construct the title with a titleName and an empty list of pages and articles
     * @param deliveryName
     * @param newspaperTitle
     * @param noOfArticles
     * @param noOfPages
     */
    public DeliveryTitleInfo(String deliveryName, String newspaperTitle, int noOfArticles, int noOfPages) {
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
        articles.add(fileName);
    }

    @XmlElement
    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public List<Page> getPages() {
        return this.pages;
    }

    public List<Article> getArticles() {
        return this.articles;
    }

    @XmlElement
    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public void addPages(Page fileName) {
        pages.add(fileName);
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

    public List<MissingItem> getMissingItems() {
        return missingItems;
    }

    @XmlElement
    public void setMissingItems(List<MissingItem> missingItems) {
        this.missingItems =missingItems;
    }
}
