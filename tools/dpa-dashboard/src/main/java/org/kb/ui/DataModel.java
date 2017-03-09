package org.kb.ui;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;

import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class DataModel {


    DomsItem o;

    public String getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(String currentEvent) {
        this.currentEvent = currentEvent;
    }

    String currentEvent;

    public void setItem(DomsItem o) {
        this.o = o;
    }

    public DomsItem getItem() {
        return o;
    }
}
