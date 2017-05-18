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

    // The name of the delivery "dl_######_rt#"
    private String deliveryName;

    // The newspapertitle Berligske, Information etc.
    private String newspaperTitle;

    // A list of articles contained in the delivery and title
    private List<Article> articles = new ArrayList<Article>();

    // A list of pages contained in the delivery and title
    private List<Page> pages = new ArrayList<Page>();

    // The number of articles in the delivery and title (As a performance optimization the lists is not allways initialized)
    private int noOfArticles;

    // The number of pages in the delivery and title (As a performance optimization the lists is not allways initialized)
    private int noOfPages;

    // Has manual check been performed
    private boolean checked = false;

    // Comment by the user if extra textual description about the check is relevant
    private String comment;

    // The initials of the person performing the check
    private String initials;

    // List of generic information about things missing in the delivery
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

    public void addPages(Page fileName) {
        pages.add(fileName);
    }

    @XmlElement
    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public List<Page> getPages() {
        return this.pages;
    }

    public void addArticle(Article fileName) {
        articles.add(fileName);
    }

    @XmlElement
    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public List<Article> getArticles() {
        return this.articles;
    }

    @XmlAttribute
    public void setNoOfArticles(int noOfArticles) {
        this.noOfArticles = noOfArticles;
    }

    public int getNoOfArticles() {
        return noOfArticles;
    }

    @XmlAttribute
    public void setNoOfPages(int noOfPages) {
        this.noOfPages = noOfPages;
    }

    public int getNoOfPages() {
        return noOfPages;
    }

    @XmlAttribute
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
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

    @XmlElement
    public void setMissingItems(List<MissingItem> missingItems) {
        this.missingItems =missingItems;
    }

    public List<MissingItem> getMissingItems() {
        return missingItems;
    }
}
