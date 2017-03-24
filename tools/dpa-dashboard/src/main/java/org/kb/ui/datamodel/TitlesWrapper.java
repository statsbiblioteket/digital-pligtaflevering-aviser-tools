package org.kb.ui.datamodel;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;

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
