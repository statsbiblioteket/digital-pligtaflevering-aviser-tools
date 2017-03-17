package org.kb.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsParser;
import org.kb.ui.FetchEventStructure;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class DataModel {

    private DomsItem domsItem;
    private String currentEvent;
    private DomsParser parser = new DomsParser();
    private String selectedDelivery;
    private String selectedTitle;
    private FetchEventStructure eventFetch = new FetchEventStructure();


    public void setParser(DomsParser parser) {
        this.parser = parser;
    }

    public String getSelectedDelivery() {
        return selectedDelivery;
    }

    public void setSelectedDelivery(String selectedDelivery) {
        this.selectedDelivery = selectedDelivery;
    }

    public String getSelectedTitle() {
        return selectedTitle;
    }

    public void setSelectedTitle(String selectedTitle) {
        this.selectedTitle = selectedTitle;
    }

    public FetchEventStructure getEventFetch() {
        return eventFetch;
    }

    public void setEventFetch(FetchEventStructure eventFetch) {
        this.eventFetch = eventFetch;
    }

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


    HashMap<String, DomsItem> deliveryList = new HashMap<String, DomsItem>();

    public void initiateDeliveries(String info) {
        Stream<DomsItem> items = eventFetch.getState(info);
        items.forEach(new Consumer<DomsItem>() {
            @Override
            public void accept(final DomsItem o) {
                deliveryList.put(o.getPath(), o);
            }
        });
    }

    public DomsItem getDeliveryFromName(String name) {
        return deliveryList.get(name);
    }

    public Set<String> getInitiatedDeliveries() {
        return deliveryList.keySet();
    }


    public ArrayList<String> getDeliveries(String info) {

        ArrayList<String> returnList = new ArrayList<String>();

        Stream<DomsItem> items = eventFetch.getState(info);


        items.forEach(new Consumer<DomsItem>() {
            @Override
            public void accept(final DomsItem o) {
                returnList.add(o.getPath());
            }
        });
        return returnList;
    }
}
