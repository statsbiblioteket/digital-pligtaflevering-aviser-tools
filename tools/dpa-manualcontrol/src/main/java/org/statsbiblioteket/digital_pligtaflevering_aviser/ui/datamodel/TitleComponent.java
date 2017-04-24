package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

/**
 * A Component containing the metadata of the files that is in a title and a delivery
 */
public class TitleComponent {
    private String sectionName;
    private String sectionNumber;
    private int pageCount;

    public TitleComponent(String sectionName, String sectionNumber, int pageCount) {
        this.sectionName = sectionName;
        this.sectionNumber = sectionNumber;
        this.pageCount = pageCount;
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
}


