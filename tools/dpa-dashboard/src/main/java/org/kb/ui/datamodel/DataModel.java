package org.kb.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsParser;
import org.kb.ui.FetchEventStructure;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class DataModel {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM");

    private DomsItem domsItem;
    private String currentEvent;
    private DomsParser parser = new DomsParser();
    private String selectedDelivery;
    private String selectedTitle;
    private String currentlySelectedMonth;
    private TitleDeliveryHierachy currentlySelectedTitleHiearachy;

    private HashMap<String, DomsItem> deliveryList = new HashMap<String, DomsItem>();
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


    public Set<String> getTitles() throws Exception {
        if(currentlySelectedTitleHiearachy==null) {
            initiateTitleHierachy();
        }

        return currentlySelectedTitleHiearachy.deliveryStructure.keySet();
    }

    public List<DeliveryIdentifier> getDeliverysFromTitle(String title) {
        return currentlySelectedTitleHiearachy.deliveryStructure.get(title).getDeliveries();
    }


    public void initiateDeliveries(String info) {
        Stream<DomsItem> items = eventFetch.getState(info);
        items.forEach(new Consumer<DomsItem>() {
            @Override
            public void accept(final DomsItem o) {
                deliveryList.put(o.getPath(), o);
            }
        });
    }

    public void writeToCurrentItemCashed(String deliveryName, String titleName, boolean checked, String comment) {
        currentlySelectedTitleHiearachy.setDeliveryTitleCheckStatus(titleName, deliveryName, checked, comment);
    }



    public void writeToCurrentItemInFedora(String deliveryName, String titleName) {

        domsItem = getDeliveryFromName(deliveryName);
        DomsItem selectedTitleItem = null;

        Iterator<DomsItem> titleSubfolder = parser.processChildDomsId().apply(domsItem);
        while(titleSubfolder.hasNext()) {
            DomsItem titleItem = titleSubfolder.next();
            String itemPath = titleItem.getPath();

            if(titleName.equals(itemPath.substring(itemPath.indexOf("/")+1))) {
                selectedTitleItem = titleItem;

                byte[] statisticsStream = "DUMMYSTREAM".getBytes();
                String settingDate = new java.util.Date().toString();

                selectedTitleItem.modifyDatastreamByValue(
                        "TESTITEM",
                        null, // no checksum
                        null, // no checksum
                        statisticsStream,
                        null,
                        "text/xml",
                        null,
                        null);
            }
        }
    }



    public void initiateTitleHierachy() throws Exception {
        currentlySelectedTitleHiearachy = new TitleDeliveryHierachy();
        Iterator<String> titles = this.getInitiatedDeliveries().iterator();

        while(titles.hasNext()) {

            String delivery = titles.next();

            final List<DomsDatastream> datastreams = this.getDeliveryFromName(delivery).datastreams();
            Optional<DomsDatastream> profileOptional = datastreams.stream()
                    .filter(ds -> ds.getId().equals("DELIVERYSTATISTICS"))
                    .findAny();


            if (profileOptional.isPresent()) {

                DomsDatastream ds = profileOptional.get();
                //We are reading this textstring as a String and are aware that thish might leed to encoding problems
                StringReader reader = new StringReader(ds.getDatastreamAsString());
                InputSource inps = new InputSource(reader);

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(inps);
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                XPathExpression expr = xpath.compile("/deliveryStatistics/titles/title/@titleName");
                NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                for(int i = 0; i< nl.getLength(); i++) {
                    String titleItem = nl.item(i).getNodeValue();

                    XPathExpression articleExpression = xpath.compile("/deliveryStatistics/titles/title[@titleName='"+titleItem+"']/articles/article");
                    NodeList articles = (NodeList) articleExpression.evaluate(doc, XPathConstants.NODESET);
                    XPathExpression pagesExpression = xpath.compile("/deliveryStatistics/titles/title[@titleName='"+titleItem+"']/pages/page");
                    NodeList pages = (NodeList) pagesExpression.evaluate(doc, XPathConstants.NODESET);

                    currentlySelectedTitleHiearachy.addDeliveryToTitle(titleItem, new DeliveryIdentifier(delivery, articles.getLength(), pages.getLength()));
                }
            }
        }
    }

    public void setSelectedMonth(Date selectedMonth) {
        currentlySelectedMonth = dateFormat.format(selectedMonth);
    }


    public void saveCurrentTitleHierachy(Date selectedMonth) throws Exception {
        currentlySelectedMonth = dateFormat.format(selectedMonth);

        /*Wrapper wrapper = tabelsLayout.getDeliveries();*/
        File tempFile = new File("/home/mmj/tools/tomcat",  currentlySelectedMonth + ".xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(TitleDeliveryHierachy.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(currentlySelectedTitleHiearachy, tempFile);
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
