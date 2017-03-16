package org.kb.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsParser;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.HashSet;

/**
 * Created by mmj on 3/2/17.
 */
public class DataModel {


    private DomsItem domsItem;
    private String currentEvent;
    private DomsParser parser = new DomsParser();

    public String getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(String currentEvent) {
        this.currentEvent = currentEvent;
    }


    public DomsParser getParser() {
        return parser;
    }

    public void setDomsItem(DomsItem o) {
        this.domsItem = domsItem;
    }

    public DomsItem getDomsItem() {
        return domsItem;
    }


    public HashSet<String> getTitles() {
        try {
            File tempFile = new File("/home/mmj/tools/tomcat", "titleList.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Wrapper.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Wrapper deserializedObject = (Wrapper)jaxbUnmarshaller.unmarshal(tempFile);
            return deserializedObject.getHashtable();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashSet<String>();
    }


}
