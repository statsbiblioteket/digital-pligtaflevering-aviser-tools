package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

/**
 * A Component containing the metadata of the files that is in a title and a delivery
 * The  component contains the information to show in the section-table
 */
public class TitleComponent {
    private String sectionName;
    private String nr;
    private int pages;

    public TitleComponent(String sectionName, String nr, int pages) {
        this.sectionName = sectionName;
        this.nr = nr;
        this.pages = pages;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getNr() {
        return nr;
    }

    public void setNr(String nr) {
        this.nr = nr;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}


