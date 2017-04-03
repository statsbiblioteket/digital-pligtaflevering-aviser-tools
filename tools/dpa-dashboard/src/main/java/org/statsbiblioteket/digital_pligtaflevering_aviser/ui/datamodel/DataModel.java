package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.DeliveryFedoraSerializer;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.DeliveryFilesystemSerializer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * DataModel has one initiated instance per browsersession
 */
public class DataModel {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM");

    private String currentEvent;
    private DeliveryIdentifier selectedDelItem;
    private String selectedDelivery;
    private String selectedTitle;
    private String currentlySelectedMonth;
    private TitleDeliveryHierachy currentlySelectedTitleHiearachy;

    private DeliveryFilesystemSerializer filesystemSerializer = new DeliveryFilesystemSerializer();

    private DeliveryFedoraSerializer fedoraSerializer = new DeliveryFedoraSerializer();



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

    public String getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(String currentEvent) {
        this.currentEvent = currentEvent;
    }





    public Set<String> getTitles() throws Exception {
        if(currentlySelectedTitleHiearachy==null) {
            initiateTitleHierachyFromFedora();
        }
        return currentlySelectedTitleHiearachy.getDeliveryStructure().keySet();
    }

    public Set<String> getTitlesFromFileSystem() throws Exception {
        if(currentlySelectedTitleHiearachy==null) {
            initiateTitleHierachyFromFilesystem();
        }
        return currentlySelectedTitleHiearachy.getDeliveryStructure().keySet();
    }

    public void selectTitleDelivery() {
        List<DeliveryIdentifier> delList = currentlySelectedTitleHiearachy.getDeliveryStructure().get(selectedTitle).getDeliveries();
        for(DeliveryIdentifier delItem : delList) {
            if(selectedDelivery.equals(delItem.getName())) {
                selectedDelItem = delItem;
            }
        }
    }

    public void addTextToCurrentItem(Page page) {
        selectedDelItem.addPages(page);
    }


    public DeliveryIdentifier getCurrentDelItem() {
        return selectedDelItem;
    }

    public List<DeliveryIdentifier> getDeliverysFromTitle(String title) {
        return currentlySelectedTitleHiearachy.getDeliveryStructure().get(title).getDeliveries();
    }


    public void initiateDeliveries(String info) {
        fedoraSerializer.initiateDeliveries(info);
    }

    public void writeToCurrentItemCashed(String deliveryName, String titleName, boolean checked, String initials, String comment) {
        try {
            DeliveryIdentifier deli = currentlySelectedTitleHiearachy.setDeliveryTitleCheckStatus(titleName, deliveryName, checked, initials, comment);

            DeliveryFilesystemSerializer.saveCurrentTitleHierachyToFilesystem(currentlySelectedMonth + "/" + titleName + "/" + deliveryName, deli);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void writeToCurrentItemInFedora(String deliveryName, String titleName) {
        fedoraSerializer.writeToCurrentItemInFedora(deliveryName, titleName);
    }


    public Title getTitleObj(String selectedDelivery, String selectedTitle) {
        return fedoraSerializer.getTitleObj(selectedDelivery, selectedTitle);
    }

    public DomsItem getItemFromUuid(String id) {
        return fedoraSerializer.getItemFromUuid(id);
    }



    public void initiateTitleHierachyFromFedora() throws Exception {

        if(currentlySelectedTitleHiearachy!=null) {
            return;
        }

        currentlySelectedTitleHiearachy = fedoraSerializer.getTitleHierachyFromFedora();
    }


    public void initiateTitleHierachyFromFilesystem() throws Exception {
        currentlySelectedTitleHiearachy = new TitleDeliveryHierachy();
        Iterator<String> titles = this.getInitiatedDeliveries().iterator();

        String currentFolder = "/home/mmj/tools/tomcat/" + currentlySelectedMonth;
        File folderForThis = new File(currentFolder);
        if(!folderForThis.exists()) {
            folderForThis.mkdir();
        }
        File[] listOfFiles = folderForThis.listFiles();

        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryIdentifier.class);
        Unmarshaller jaxbUnMarshaller = jaxbContext.createUnmarshaller();

        for(File titleFolder : listOfFiles) {
            File[] listOfXmls = titleFolder.listFiles();
            for(File xmlFile : listOfXmls) {
                DeliveryIdentifier deli = (DeliveryIdentifier)jaxbUnMarshaller.unmarshal(xmlFile);
                currentlySelectedTitleHiearachy.addDeliveryToTitle(titleFolder.getName(), deli);
            }
        }
    }


    public void setSelectedMonth(Date selectedMonth) {
        currentlySelectedMonth = dateFormat.format(selectedMonth);
    }


    public void saveCurrentTitleHierachyToFilesystem(Date selectedMonth) throws Exception {
        currentlySelectedMonth = dateFormat.format(selectedMonth);
        DeliveryFilesystemSerializer.saveCurrentTitleHierachyToFilesystem(currentlySelectedMonth, currentlySelectedTitleHiearachy);
    }


    public DomsItem getDeliveryFromName(String name) {
        return fedoraSerializer.getDeliveryFromName(name);
    }

    public Set<String> getInitiatedDeliveries() {
        return fedoraSerializer.getInitiatedDeliveries();
    }

}
