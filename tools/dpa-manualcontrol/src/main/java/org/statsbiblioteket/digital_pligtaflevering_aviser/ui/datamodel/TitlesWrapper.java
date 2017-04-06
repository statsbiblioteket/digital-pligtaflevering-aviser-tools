package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TitlesWrapper {

    private TitleDeliveryHierachy hashtable;

    public TitleDeliveryHierachy getHashtable() {
        return hashtable;
    }

    public void setHashtable(TitleDeliveryHierachy hashtable) {
        this.hashtable = hashtable;
    }

}
