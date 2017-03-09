package org.kb.ui.tableBeans;

/**
 * Created by mmj on 3/9/17.
 */
public class TitleComponent {
    String titleName;
    int articleCount;
    int pageCount;

    public TitleComponent(String titleName, int articleCount, int pageCount) {
        this.titleName = titleName;
        this.articleCount = articleCount;
        this.pageCount = pageCount;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}
