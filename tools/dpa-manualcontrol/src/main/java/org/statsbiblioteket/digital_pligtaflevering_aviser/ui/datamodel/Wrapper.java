package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import java.util.HashSet;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Wrapper {

    private HashSet<String> hashtable;

    public HashSet<String> getHashtable() {
        return hashtable;
    }

    public void setHashtable(HashSet<String> hashtable) {
        this.hashtable = hashtable;
    }

}
