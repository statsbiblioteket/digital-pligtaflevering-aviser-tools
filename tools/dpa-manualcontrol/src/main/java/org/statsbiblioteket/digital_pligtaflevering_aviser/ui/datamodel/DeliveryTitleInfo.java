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
    private String title;

    // A list of articleList contained in the delivery and title
    
    private List<Article> articleList = new ArrayList<Article>();

    // A list of pageList contained in the delivery and title
    private List<Page> pageList = new ArrayList<Page>();

    // The number of articleList in the delivery and title (As a performance optimization the lists is not allways initialized)
    private int articles;

    // The number of pageList in the delivery and title (As a performance optimization the lists is not allways initialized)
    private int pages;

    // Has manual check been performed
    private boolean chk = false;

    // Comment by the user if extra textual description about the check is relevant
    private String comment;

    // The initials of the person performing the check
    private String initials;

    // List of generic information about things missing in the delivery
    private List<MissingItem> missingItems = new ArrayList<MissingItem>();

    public DeliveryTitleInfo() {
    }

    /**
     * construct the title with a titleName and an empty list of pageList and articleList
     * @param deliveryName
     * @param title
     * @param articles
     * @param pages
     */
    public DeliveryTitleInfo(String deliveryName, String title, int articles, int pages) {
        this.deliveryName = deliveryName;
        this.title = title;
        this.articles = articles;
        this.pages = pages;
    }

    @XmlAttribute
    public void setDeliveryName(String deliveryName) {
        this.deliveryName = deliveryName;
    }

    public String getDeliveryName() {
        return this.deliveryName;
    }

    @XmlAttribute(name = "newspaperTitle")
    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void addPages(Page fileName) {
        pageList.add(fileName);
    }

    @XmlElement(name = "pages")
    public void setPageList(List<Page> pageList) {
        this.pageList = pageList;
    }

    public List<Page> getPageList() {
        return this.pageList;
    }

    public void addArticle(Article fileName) {
        articleList.add(fileName);
    }

    @XmlElement(name = "articles")
    public void setArticleList(List<Article> articleList) {
        this.articleList = articleList;
    }

    public List<Article> getArticleList() {
        return this.articleList;
    }
    
    @XmlAttribute(name="noOfArticles")
    public void setArticles(int articles) {
        this.articles = articles;
    }

    public int getArticles() {
        return articles;
    }

    @XmlAttribute(name="noOfPages")
    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getPages() {
        return pages;
    }

    @XmlAttribute(name="checked")
    public void setChk(boolean chk) {
        this.chk = chk;
    }

    public boolean isChk() {
        return chk;
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
        this.missingItems = missingItems;
    }

    public List<MissingItem> getMissingItems() {
        return missingItems;
    }
}
