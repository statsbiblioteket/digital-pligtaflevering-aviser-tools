package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.DeliveryFedoraSerializer;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.DeliveryFilesystemSerializer;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.RepositoryProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * DataModel has one initiated instance per browsersession.
 * The datamodel contains cashed information about the deliveries, which is currently beeing checked
 */
public class DataModel {
    protected Logger log = LoggerFactory.getLogger(getClass());

    private ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties("/backend.properties");
    private DomsRepository repository = new RepositoryProvider().apply(map);

    //Formatter for naming the cashing folder by the date
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");

    private DeliveryTitleInfo selectedDelItem;
    private String selectedDelivery;
    private String selectedTitle;
    private String selectedSection = null;
    private String currentlySelectedMonth;
    private TitleDeliveryHierachy currentlySelectedTitleHiearachy;

    private DeliveryFilesystemSerializer filesystemSerializer;

    private DeliveryFedoraSerializer fedoraSerializer;

    public DataModel() {
        String cashingPath = map.getDefault("dpa.manualcontrol.cashingfolder", "/tmp");
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

    public List<DeliveryTitleInfo> getDeliveryTitleObjects() {
        return currentlySelectedTitleHiearachy.getDeliveryTitleObjects(selectedDelivery);
    }

    public void initiateDeliveries(boolean allreadyValidated) {
        DeliveryFedoraSerializer.EventStatus evtStatus = DeliveryFedoraSerializer.EventStatus.READYFORMANUALCHECK;
        if(allreadyValidated) {
            evtStatus = DeliveryFedoraSerializer.EventStatus.DONEMANUALCHECK;
        }
        fedoraSerializer.initiateDeliveries(evtStatus, "dl_" + currentlySelectedMonth);
    }

    /**
     * Write information to the defined DeliveryTitleInfo
     * @param deliveryName
     * @param titleName
     * @param checked
     * @param initials
     * @param comment
     * @param missingItems
     * @return
     */
    public boolean writeToCurrentItemCashed(String deliveryName, String titleName, boolean checked, String initials, String comment, List<MissingItem> missingItems) {
        try {
            DeliveryTitleInfo deli = currentlySelectedTitleHiearachy.setDeliveryTitleCheckStatus(titleName, deliveryName, checked, initials, comment, missingItems);
            filesystemSerializer.saveDeliveryToFilesystem(currentlySelectedMonth, deli);
            return fedoraSerializer.writeDeliveryToFedora(deli);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Remove a specific cashed title in a delivery
     * @throws Exception
     */
    public void removeCurrentSelectedTitleInDelivery() throws Exception {
        filesystemSerializer.removeDeliveryFromFilesystem(currentlySelectedMonth, selectedDelivery, selectedTitle);
    }

    /**
     * Get the Title object from fedora, constructed with the STATISTICS stream
     * @param selectedDelivery
     * @param selectedTitle
     * @return
     */
    public Title getTitleObj(String selectedDelivery, String selectedTitle) {
        return fedoraSerializer.getTitleObj(selectedDelivery, selectedTitle);
    }

    /**
     * Get the domsItem from the uuid. The Item is fetched directly from fedora
     * @param id
     * @return
     */
    public DomsItem getItemFromUuid(String id) {
        return fedoraSerializer.getItemFromUuid(id);
    }

    /**
     * Initiate TitleDeliveryHierachy from fedora, and cash it in the model
     * @throws Exception
     */
    public void initiateTitleHierachyFromFedora() throws Exception {
        currentlySelectedTitleHiearachy = fedoraSerializer.getTitleHierachyFromFedora();
    }

    /**
     * Initiate TitleDeliveryHierachy from filesystem, and cash it in the model
     * @throws Exception
     */
    public void initiateTitleHierachyFromFilesystem() throws Exception {
        currentlySelectedTitleHiearachy = filesystemSerializer.initiateTitleHierachyFromFilesystem(currentlySelectedMonth);
    }

    /**
     * Set the month to the model, the month is used as basis of which deliveries that can currently get validated
     * @param selectedMonth
     */
    public void setSelectedMonth(Date selectedMonth) {
        currentlySelectedMonth = dateFormat.format(selectedMonth);
    }


    public boolean saveCurrentTitleHierachyToFilesystem() throws Exception {
        return filesystemSerializer.saveDeliveryToFilesystem(currentlySelectedMonth, currentlySelectedTitleHiearachy);
    }

    /**
     * Is the month been cashed ti the filesystem
     * @return
     */
    public boolean isMonthInitiated() {
        return filesystemSerializer.isMonthInitiated(currentlySelectedMonth);
    }


    public DomsItem getDeliveryFromName(String name) {
        return fedoraSerializer.getDeliveryFromName(name);
    }

    public Set<String> getInitiatedDeliveries() {
        return fedoraSerializer.getInitiatedDeliveries();
    }

}
