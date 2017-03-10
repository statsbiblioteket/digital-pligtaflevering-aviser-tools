package org.kb.ui.tableBeans;

/**
 * Created by mmj on 3/9/17.
 */
public class FileComponent {
    private String fileName;
    private String titleName;
    private String sectionName;

    public FileComponent(String fileName, String titleName, String sectionName) {
        this.fileName = titleName;
        this.titleName = titleName;
        this.sectionName = sectionName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
}


