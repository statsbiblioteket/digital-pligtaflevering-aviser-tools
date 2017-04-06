package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

/**
 * Created by mmj on 3/9/17.
 */
public class TitleComponent {
    private String sectionName;
    private String sectionNumber;
    private int pageCount;
    private int articleCount;

    public TitleComponent(String sectionName, String sectionNumber, int pageCount, int articleCount) {
        this.sectionName = sectionName;
        this.sectionNumber = sectionNumber;
        this.pageCount = pageCount;
        this.articleCount = articleCount;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }
}


