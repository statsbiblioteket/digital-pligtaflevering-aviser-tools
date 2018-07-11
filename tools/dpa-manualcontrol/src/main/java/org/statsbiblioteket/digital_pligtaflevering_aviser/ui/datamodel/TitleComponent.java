package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

/**
 * A Component containing the metadata of the files that is in a title and a delivery
 * The  component contains the information to show in the section-table
 */
public class TitleComponent {
    private String sectionName;
    private int sectionNumber;
    private int pageCount;

    public TitleComponent(String sectionName, int sectionNumber, int pageCount) {
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

    public int getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(int sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}


