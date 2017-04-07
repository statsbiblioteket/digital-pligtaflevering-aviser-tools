package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.DeliveryFedoraSerializer;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.DeliveryFilesystemSerializer;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.FetchEventStructure;

import java.text.SimpleDateFormat;
import java.util.Date;
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
    private String selectedSection = null;
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

    public String getSelectedSection() {
        return selectedSection;
    }

    public void setSelectedSection(String selectedSection) {
        this.selectedSection = selectedSection;
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





    public List<String> getTitlesFromFileSystem() throws Exception {
        if(currentlySelectedTitleHiearachy==null) {
            initiateTitleHierachyFromFilesystem();
        }

        return currentlySelectedTitleHiearachy.getAllTitles();
    }

    public void selectTitleDelivery() {

        selectedDelItem = currentlySelectedTitleHiearachy.getDeliveryTitleCheckStatus(selectedTitle, selectedDelivery);
    }

    public void addCheckedPage(Page page) {
        selectedDelItem.addPages(page);
    }

    public void addCheckedArticle(Article article) {
        selectedDelItem.addArticle(article);
    }


    public DeliveryIdentifier getCurrentDelItem() {
        return selectedDelItem;
    }

    public List<DeliveryIdentifier> getDeliverysFromTitle(String title) {
        return currentlySelectedTitleHiearachy.getDeliverysFromTitle(title);
    }

    public List<DeliveryIdentifier> getOtherFromDelivery() {
        return currentlySelectedTitleHiearachy.getOtherStructure(selectedDelivery);
    }

    public void initiateDeliveries(boolean allreadyValidated) {
        FetchEventStructure.EventStatus evtStatus = FetchEventStructure.EventStatus.READYFORMANUALCHECK;
        if(allreadyValidated) {
            evtStatus = FetchEventStructure.EventStatus.DONEMANUALCHECK;
        }
        fedoraSerializer.initiateDeliveries(evtStatus);
    }

    public void writeToCurrentItemCashed(String deliveryName, String titleName, boolean checked, String initials, String comment) {
        try {
            DeliveryIdentifier deli = currentlySelectedTitleHiearachy.setDeliveryTitleCheckStatus(titleName, deliveryName, checked, initials, comment);

            filesystemSerializer.saveCurrentTitleHierachyToFilesystem(currentlySelectedMonth, deli);
            fedoraSerializer.writeStuff(deli);




        } catch (Exception e) {
            e.printStackTrace();
        }
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
        currentlySelectedTitleHiearachy = filesystemSerializer.initiateTitleHierachyFromFilesystem(currentlySelectedMonth);
    }


    public void setSelectedMonth(Date selectedMonth) {
        currentlySelectedMonth = dateFormat.format(selectedMonth);
    }


    public boolean saveCurrentTitleHierachyToFilesystem(Date selectedMonth) throws Exception {
        currentlySelectedMonth = dateFormat.format(selectedMonth);
        return filesystemSerializer.saveCurrentTitleHierachyToFilesystem(currentlySelectedMonth, currentlySelectedTitleHiearachy);
    }

    public boolean isMonthInitiated(Date selectedMonth) {
        currentlySelectedMonth = dateFormat.format(selectedMonth);
        return filesystemSerializer.isMonthInitiated(currentlySelectedMonth);
    }


    public DomsItem getDeliveryFromName(String name) {
        return fedoraSerializer.getDeliveryFromName(name);
    }

    public Set<String> getInitiatedDeliveries() {
        return fedoraSerializer.getInitiatedDeliveries();
    }

}
