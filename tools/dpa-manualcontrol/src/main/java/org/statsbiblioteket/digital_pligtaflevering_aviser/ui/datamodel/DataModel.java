package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.DeliveryFedoraSerializer;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.DeliveryFilesystemSerializer;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.FetchEventStructure;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.RepositoryProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * DataModel has one initiated instance per browsersession
 */
public class DataModel {

    private ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties("/backend.properties");
    private DomsRepository repository = new RepositoryProvider().apply(map);

    //Formatter for cashing folder
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM");

    private DeliveryTitleInfo selectedDelItem;
    private String selectedDelivery;
    private String selectedTitle;
    private String selectedSection = null;
    private String currentlySelectedMonth;
    private TitleDeliveryHierachy currentlySelectedTitleHiearachy;

    private DeliveryFilesystemSerializer filesystemSerializer;

    private DeliveryFedoraSerializer fedoraSerializer;

    public DataModel() {
        String cashingPath = map.getDefault("dpa.manualcontrol.cashingfolder", "dummy");
        filesystemSerializer = new DeliveryFilesystemSerializer(cashingPath);
        fedoraSerializer = new DeliveryFedoraSerializer(repository);
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

    public String getSelectedSection() {
        return selectedSection;
    }

    public void setSelectedSection(String selectedSection) {
        this.selectedSection = selectedSection;
    }

    public void setSelectedTitle(String selectedTitle) {
        this.selectedTitle = selectedTitle;
    }


    public List<String> getTitlesFromFileSystem() throws Exception {
        initiateTitleHierachyFromFilesystem();
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


    public DeliveryTitleInfo getCurrentDelItem() {
        return selectedDelItem;
    }

    public List<DeliveryTitleInfo> getDeliverysFromTitle(String title) {
        return currentlySelectedTitleHiearachy.getDeliverysFromTitle(title);
    }

    public List<DeliveryTitleInfo> getOtherFromDelivery() {
        return currentlySelectedTitleHiearachy.getOtherStructure(selectedDelivery);
    }

    public void initiateDeliveries(boolean allreadyValidated) {
        FetchEventStructure.EventStatus evtStatus = FetchEventStructure.EventStatus.READYFORMANUALCHECK;
        if(allreadyValidated) {
            evtStatus = FetchEventStructure.EventStatus.DONEMANUALCHECK;
        }
        fedoraSerializer.initiateDeliveries(evtStatus);
    }

    /**
     * Write information to the defined DeliveryTitleInfo
     * @param deliveryName
     * @param titleName
     * @param checked
     * @param initials
     * @param comment
     * @param missingItems
     */
    public void writeToCurrentItemCashed(String deliveryName, String titleName, boolean checked, String initials, String comment, List<MissingItem> missingItems) {
        try {
            DeliveryTitleInfo deli = currentlySelectedTitleHiearachy.setDeliveryTitleCheckStatus(titleName, deliveryName, checked, initials, comment, missingItems);

            filesystemSerializer.saveDeliveryToFilesystem(currentlySelectedMonth, deli);
            fedoraSerializer.writeStuff(deli);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a specific cashed title in a delivery
     * @throws Exception
     */
    public void removeCurrentSelectedTitleInDelivery() throws Exception {
        filesystemSerializer.removeDeliveryFromFilesystem(currentlySelectedMonth, selectedDelivery, selectedTitle);
    }

    public Title getTitleObj(String selectedDelivery, String selectedTitle) {
        return fedoraSerializer.getTitleObj(selectedDelivery, selectedTitle);
    }

    public DomsItem getItemFromUuid(String id) {
        return fedoraSerializer.getItemFromUuid(id);
    }



    public void initiateTitleHierachyFromFedora() throws Exception {
        currentlySelectedTitleHiearachy = fedoraSerializer.getTitleHierachyFromFedora();
    }


    public void initiateTitleHierachyFromFilesystem() throws Exception {
        currentlySelectedTitleHiearachy = filesystemSerializer.initiateTitleHierachyFromFilesystem(currentlySelectedMonth);
    }


    public void setSelectedMonth(Date selectedMonth) {
        currentlySelectedMonth = dateFormat.format(selectedMonth);
    }


    public boolean saveCurrentTitleHierachyToFilesystem() throws Exception {
        return filesystemSerializer.saveDeliveryToFilesystem(currentlySelectedMonth, currentlySelectedTitleHiearachy);
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
