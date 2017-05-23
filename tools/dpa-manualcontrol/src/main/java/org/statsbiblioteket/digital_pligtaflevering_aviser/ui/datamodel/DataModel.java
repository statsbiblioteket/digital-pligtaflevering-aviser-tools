package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.NewspaperContextListener;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.DeliveryFedoraCommunication;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.DeliveryFilesystemReadWrite;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.RepositoryProvider;

import java.text.ParseException;
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

    private ConfigurationMap map = NewspaperContextListener.configurationmap;
    private DomsRepository repository = new RepositoryProvider().apply(map);

    //Formatter for naming the cashing folder by the date
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
    private boolean includeValidatedDeliveries = false;
    private String initials;
    private DeliveryTitleInfo selectedDelItem;
    private String selectedDelivery;
    private String selectedTitle;
    private String selectedSection = null;
    private String currentlySelectedMonth;
    private TitleDeliveryHierarchy currentlySelectedTitleHiearachy;

    private DeliveryFilesystemReadWrite filesystemReadWrite;

    private DeliveryFedoraCommunication fedoraCommunication;

    public DataModel() {
        String cashingfolder = map.getRequired("dpa.manualcontrol.cashingfolder");
        filesystemReadWrite = new DeliveryFilesystemReadWrite(cashingfolder);
        fedoraCommunication = new DeliveryFedoraCommunication(map.getRequired("autonomous.itemTypes"),
                map.getRequired("autonomous.pastSuccessfulEvents"),
                map.getRequired("autonomous.thisEvent"),
                repository);
    }

    public void cleanModel() {
        selectedDelItem = null;
        selectedDelivery = null;
        selectedTitle = null;
        selectedSection = null;
        currentlySelectedMonth = null;
        currentlySelectedTitleHiearachy = null;
    }

    /**
     * Set initials of the person currently using the application in this browserinstance
     * @param initials
     */
    public void setInitials(String initials) {
        this.initials = initials;
    }

    public void setIncludeValidatedDeliveries(boolean includeValidatedDeliveries) {
        this.includeValidatedDeliveries = includeValidatedDeliveries;
    }


    /**
     * Get initials of the person currently using the application in this browserinstance
     * @return
     */
    public String getInitials() {
        return initials;
    }

    /**
     * Get the name of the delivery which is currently in operation
     * @return
     */
    public String getSelectedDelivery() {
        return selectedDelivery;
    }

    /**
     * Set the name of the delivery which is currently in operation
     * @param selectedDelivery
     */
    public void setSelectedDelivery(String selectedDelivery) {
        this.selectedDelivery = selectedDelivery;
    }

    /**
     * Get the name of the newspapertitle which is currently in operation
     * @return
     */
    public String getSelectedTitle() {
        return selectedTitle;
    }

    /**
     * Get the name of the newspapersection which is currently in operation
     * @return
     */
    public String getSelectedSection() {
        return selectedSection;
    }

    /**
     * Set the name of the newspapersection which is currently in operation
     * @param selectedSection
     */
    public void setSelectedSection(String selectedSection) {
        this.selectedSection = selectedSection;
    }

    /**
     * Set the name of the delivery which is currently in operation
     * @param selectedTitle
     */
    public void setSelectedTitle(String selectedTitle) {
        this.selectedTitle = selectedTitle;
    }


    public List<String> getTitlesFromFileSystem() throws Exception {
        initiateTitleHierachyFromFilesystem();
        return currentlySelectedTitleHiearachy.getAllTitles();
    }

    /**
     * Select the TitleDeliveryItem for operation, selection is done with the local parameters of title and deliveryname
     */
    public void selectTitleDelivery() {
        selectedDelItem = currentlySelectedTitleHiearachy.getDeliveryTitleCheckStatus(selectedTitle, selectedDelivery);
    }

    /**
     * Add information about a newspaperpage, which has been checked
     * @param page
     */
    public void addCheckedPage(Page page) {
        selectedDelItem.addPages(page);
    }

    /**
     * Add information about a newspaperarticle, which has been checked
     * @param article
     */
    public void addCheckedArticle(Article article) {
        selectedDelItem.addArticle(article);
    }

    /**
     * Get the deliveryTitle which is currently selected for operation
     * @return
     */
    public DeliveryTitleInfo getCurrentDelItem() {
        return selectedDelItem;
    }

    /**
     * Get all deliverysTitles which contain the title-name in the parameter
     * @param title
     * @return
     */
    public List<DeliveryTitleInfo> getDeliverysFromTitle(String title) {
        return currentlySelectedTitleHiearachy.getDeliverysFromTitle(title);
    }

    /**
     * Get all deliverysTitles, that is delivered in the currently selected delivery
     * @return
     */
    public List<DeliveryTitleInfo> getDeliveryTitleObjects() {
        return currentlySelectedTitleHiearachy.getDeliveryTitleObjects(selectedDelivery);
    }

    /**
     * Initiate the list of deliveries which is currently beeing in operation
     */
    public void initiateDeliveries() {
        DeliveryFedoraCommunication.EventStatus evtStatus = DeliveryFedoraCommunication.EventStatus.READYFORMANUALCHECK;
        if(this.includeValidatedDeliveries) {
            evtStatus = DeliveryFedoraCommunication.EventStatus.DONEMANUALCHECK;
        }
        fedoraCommunication.initiateDeliveries(evtStatus, "dl_" + currentlySelectedMonth);
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
            filesystemReadWrite.saveDeliveryToFilesystem(currentlySelectedMonth, deli);
            return fedoraCommunication.writeDeliveryToFedora(deli);
        } catch (Exception e) {
            log.error("Exception occoured during writing DeliveryTitleInfo", e);
        }
        return false;
    }

    /**
     * Remove a specific cashed title in a delivery
     * @throws Exception
     */
    public void removeCurrentSelectedTitleInDelivery() throws Exception {
        filesystemReadWrite.removeDeliveryFromFilesystem(currentlySelectedMonth, selectedDelivery, selectedTitle);
    }

    /**
     * Get the Title object from fedora, constructed with the STATISTICS stream
     * @param selectedDelivery
     * @param selectedTitle
     * @return
     */
    public Title getTitleObj(String selectedDelivery, String selectedTitle) {
        return fedoraCommunication.getTitleObj(selectedDelivery, selectedTitle);
    }

    /**
     * Get the domsItem from the uuid. The Item is fetched directly from fedora
     * @param id
     * @return
     */
    public DomsItem getItemFromUuid(String id) {
        return fedoraCommunication.getItemFromUuid(id);
    }

    /**
     * Initiate TitleDeliveryHierarchy from fedora, and cash it in the model
     * @throws Exception
     */
    public void initiateTitleHierachyFromFedora() throws Exception {
        currentlySelectedTitleHiearachy = fedoraCommunication.getTitleHierachyFromFedora();
    }

    /**
     * Initiate TitleDeliveryHierarchy from filesystem, and cash it in the model
     * @throws Exception
     */
    public void initiateTitleHierachyFromFilesystem() throws Exception {
        currentlySelectedTitleHiearachy = filesystemReadWrite.initiateTitleHierachyFromFilesystem(currentlySelectedMonth);
    }

    /**
     * Set the month to the model, the month is used as basis of which deliveries that can currently get validated
     * @param selectedMonth
     */
    public void setSelectedMonth(Date selectedMonth) {
        //When the selected month is changed the model is cleaned, since the model is allways initiated with data for an entire month
        cleanModel();
        currentlySelectedMonth = dateFormat.format(selectedMonth);
    }

    public void setSelectedMonth(String selectedMonth) {
        //When the selected month is changed the model is cleaned, since the model is allways initiated with data for an entire month
        cleanModel();
        currentlySelectedMonth = selectedMonth;
    }

    /**
     * Get the currently selected month from the month as a String
     * @return
     * @throws ParseException
     */
    public Date getSelectedMonth() throws ParseException {
        if(currentlySelectedMonth == null) {
            return new Date();
        } else {
            return dateFormat.parse(currentlySelectedMonth);
        }
    }

    /**
     * Get the currently selected month as a string
     * @return
     */
    public String getSelectedMonthString() {
        return currentlySelectedMonth;
    }

    /**
     * Save the hierachy of titles and deliveries to the filesystem
     * @return
     * @throws Exception
     */
    public boolean saveCurrentTitleHierachyToFilesystem() throws Exception {
        return filesystemReadWrite.saveDeliveryToFilesystem(currentlySelectedMonth, currentlySelectedTitleHiearachy);
    }

    /**
     * Is the month been cashed ti the filesystem
     * @return
     */
    public boolean isMonthInitiated() {
        return filesystemReadWrite.isMonthInitiated(currentlySelectedMonth);
    }

    /**
     * Get the DomsItem from the name of the delivery, the domsItem can be used for reading and writing to doms
     * @param name
     * @return
     */
    public DomsItem getDeliveryFromName(String name) {
        return fedoraCommunication.getDeliveryFromName(name);
    }

    /**
     * Get a list of alle deliveries, which is currently beeing operated
     * @return
     */
    public Set<String> getInitiatedDeliveries() {
        return fedoraCommunication.getInitiatedDeliveries();
    }

}
