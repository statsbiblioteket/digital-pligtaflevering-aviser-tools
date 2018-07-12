package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import com.vaadin.server.StreamResource;
import com.vaadin.ui.Notification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.ConfirmationState;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.NewspaperContextListener;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.DeliveryFedoraCommunication;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.DeliveryFilesystemReadWrite;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.RepositoryProvider;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DataModel has one initiated instance per browsersession. The datamodel contains cashed information about the
 * deliveries, which is currently beeing checked
 */
public class DataModel {
    protected Logger log = LoggerFactory.getLogger(getClass());

    private ConfigurationMap map = NewspaperContextListener.configurationmap;
    private DomsRepository repository = new RepositoryProvider().apply(map);

    //Formatter for naming the cashing folder by the date
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
    private DeliveryFedoraCommunication.EventStatus eventStatus = DeliveryFedoraCommunication.EventStatus.READYFORMANUALCHECK;
    private String initials;
    private DeliveryTitleInfo selectedDelItem;
    private String selectedDelivery;
    private String selectedTitle;
    private String selectedSection = null;
    private String currentlySelectedMonth;
    private TitleDeliveryHierarchy currentlySelectedTitleHierarchy;
    private DeliveryPattern deliveryPattern = new DeliveryPattern();
    private DeliveryFilesystemReadWrite filesystemReadWrite;

    private DeliveryFedoraCommunication fedoraCommunication;

    public DataModel() {
        String cashingfolder = map.getRequired("dpa.manualcontrol.cashingfolder");
        Settings.expectedEvents = map.getRequired("autonomous.pastSuccessfulEvents").split(",");
        createDeliveryPattern();
        filesystemReadWrite = new DeliveryFilesystemReadWrite(cashingfolder);
        fedoraCommunication = new DeliveryFedoraCommunication(map.getRequired("autonomous.itemTypes"),
                map.getRequired("autonomous.pastSuccessfulEvents"),
                map.getRequired("autonomous.minimalpastSuccessfulEvents"),
                map.getRequired("autonomous.thisEvent"),
                repository);
    }

    private void createDeliveryPattern() {
        InputStream is = null;
        try {
            String deliveryPatternPath = map.getRequired("dpa.manualcontrol.configpath") + "/DeliveryPattern.xml";
            is = new FileInputStream(deliveryPatternPath);
            JAXBContext jaxbContext1 = JAXBContext.newInstance(DeliveryPattern.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
            deliveryPattern = (DeliveryPattern) jaxbUnmarshaller.unmarshal(is);
        } catch(JAXBException | FileNotFoundException e) {
            log.error(e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    public DeliveryPattern getDeliveryPattern() {
        return deliveryPattern;
    }


    public void cleanModel() {
        selectedDelItem = null;
        selectedDelivery = null;
        selectedTitle = null;
        selectedSection = null;
        currentlySelectedMonth = null;
        currentlySelectedTitleHierarchy = null;
    }

    /**
     * Set initials of the person currently using the application in this browserinstance
     *
     * @param initials
     */
    public void setInitials(String initials) {
        this.initials = initials;
    }

    public void setIncludeValidatedDeliveries(DeliveryFedoraCommunication.EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    public DeliveryFedoraCommunication.EventStatus getIncludeValidatedDeliveries() {
        return this.eventStatus;
    }

    /**
     * Get initials of the person currently using the application in this browserinstance
     *
     * @return
     */
    public String getInitials() {
        return initials;
    }

    /**
     * Get the name of the delivery which is currently in operation
     *
     * @return
     */
    public String getSelectedDelivery() {
        return selectedDelivery;
    }

    /**
     * Set the name of the delivery which is currently in operation
     *
     * @param selectedDelivery
     */
    public void setSelectedDelivery(String selectedDelivery) {
        this.selectedDelivery = selectedDelivery;
    }

    /**
     * Get the name of the newspapertitle which is currently in operation
     *
     * @return
     */
    public String getSelectedTitle() {
        return selectedTitle;
    }

    /**
     * Get the name of the newspapersection which is currently in operation
     *
     * @return
     */
    public String getSelectedSection() {
        return selectedSection;
    }

    /**
     * Set the name of the newspapersection which is currently in operation
     *
     * @param selectedSection
     */
    public void setSelectedSection(String selectedSection) {
        this.selectedSection = selectedSection;
    }

    /**
     * Set the name of the delivery which is currently in operation
     *
     * @param selectedTitle
     */
    public void setSelectedTitle(String selectedTitle) {
        this.selectedTitle = selectedTitle;
    }

    public List<String> getTitlesFromFileSystem() throws Exception {
        initiateTitleHierachyFromFilesystem();
        return currentlySelectedTitleHierarchy.getAllTitles();
    }

    /**
     * Select the TitleDeliveryItem for operation, selection is done with the local parameters of title and
     * deliveryname
     */
    public void selectTitleDelivery() {
        selectedDelItem = currentlySelectedTitleHierarchy.getDeliveryTitleCheckStatus(selectedTitle, selectedDelivery);
    }

    /**
     * Create a web-recourse for downloading a report of the status of manual controls of newspapers
     * @return
     */
    public StreamResource createReportResource() {
        return new StreamResource(
                new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                InputStream in = null;
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
                {
                    for (String title : currentlySelectedTitleHierarchy.getAllTitles()) {
                        baos.write((title+";\n").getBytes());
                        List<DeliveryTitleInfo> DeliveryTitleInfos = currentlySelectedTitleHierarchy.getDeliverysFromTitle(title);
                        for (DeliveryTitleInfo deliveryTitleInfo : DeliveryTitleInfos) {

                            baos.write((Optional.ofNullable(deliveryTitleInfo.getDeliveryName()).orElse("")+",").getBytes());
                            baos.write((Optional.ofNullable(deliveryTitleInfo.getInitials()).orElse("")+",").getBytes());
                            baos.write((deliveryTitleInfo.isChecked()+",").getBytes());
                            baos.write((deliveryTitleInfo.getNoOfArticles()+",").getBytes());
                            baos.write((deliveryTitleInfo.getNoOfPages()+",").getBytes());

                            String missingItemString = "";
                            List<Page> filteredPages = deliveryTitleInfo.getPages().stream().filter(p -> ConfirmationState.REJECTED.equals(p.getCheckedState())).collect(Collectors.toList());
                            for(Page filteredPage : filteredPages) {
                                missingItemString = missingItemString.concat(filteredPage.getPageName()).concat("-");
                            }
                            List<Article> filteredarticles = deliveryTitleInfo.getArticles().stream().filter(p -> ConfirmationState.REJECTED.equals(p.getCheckedState())).collect(Collectors.toList());
                            for(Article filteredArticle : filteredarticles) {
                                missingItemString = missingItemString.concat(filteredArticle.getArticleName()).concat("-");
                            }

                            baos.write((missingItemString+",").getBytes());
                            baos.write((Optional.ofNullable(deliveryTitleInfo.getComment()).orElse("").replaceAll(","," ").replaceAll(";", " ")+",").getBytes());
                            baos.write(";\n".getBytes());
                        }
                    }

                    byte[] bytes = baos.toByteArray();

                    in = new ByteArrayInputStream(bytes);

                } catch (Exception e) {
                    Notification.show("The report can not get generated", Notification.Type.ERROR_MESSAGE);
                    log.error("ERROR EXPORTING DATA", e.getMessage());
                }
                return in;
            }
        }, "export.csv");
    }

    /**
     * Add information about a newspaperpage, which has been checked
     *
     * @param page
     */
    public void addCheckedPage(Page page) {
        selectedDelItem.addPages(page);
    }

    /**
     * Add information about a newspaperarticle, which has been checked
     *
     * @param article
     */
    public void addCheckedArticle(Article article) {
        selectedDelItem.addArticle(article);
    }

    /**
     * Get the deliveryTitle which is currently selected for operation
     *
     * @return
     */
    public DeliveryTitleInfo getCurrentDelItem() {
        return selectedDelItem;
    }

    /**
     * Get all deliverysTitles which contain the title-name in the parameter
     *
     * @param title
     * @return
     */
    public List<DeliveryTitleInfo> getDeliverysFromTitle(String title) {
        return currentlySelectedTitleHierarchy.getDeliverysFromTitle(title);
    }

    /**
     * Get all deliverysTitles, that is delivered in the currently selected delivery
     *
     * @return
     */
    public List<DeliveryTitleInfo> getDeliveryTitleObjects() {
        return currentlySelectedTitleHierarchy.getDeliveryTitleObjects(selectedDelivery);
    }

    /**
     * Initiate the list of deliveries which is currently beeing in operation
     */
    public void initiateDeliveries() {
        fedoraCommunication.initiateDeliveries(eventStatus, "dl_" + currentlySelectedMonth);
    }

    /**
     * Write information to the defined DeliveryTitleInfo
     *
     * @param deliveryName
     * @param titleName
     * @param checked
     * @param initials
     * @param comment
     * @param missingItems
     * @return
     */
    public boolean writeToCurrentItemCached(String deliveryName, String titleName, boolean checked, String initials, String comment, List<MissingItem> missingItems, boolean force) {
        try {
            DeliveryTitleInfo deli = currentlySelectedTitleHierarchy.setDeliveryTitleCheckStatus(titleName, deliveryName, checked, initials, comment, missingItems);
            filesystemReadWrite.saveDeliveryToFilesystem(currentlySelectedMonth, deli);
            return fedoraCommunication.writeDeliveryToFedora(deli, force);
        } catch (Exception e) {
            log.error("Exception occoured during writing DeliveryTitleInfo", e);
        }
        return false;
    }

    /**
     * Remove a specific caced title in a delivery
     *
     * @throws Exception
     */
    public void removeCurrentSelectedTitleInDelivery() throws Exception {
        filesystemReadWrite.removeDeliveryFromFilesystem(currentlySelectedMonth, selectedDelivery, selectedTitle);
    }

    /**
     * Get the Title object from fedora, constructed with the STATISTICS stream
     *
     * @param selectedDelivery
     * @param selectedTitle
     * @return
     */
    public Title getTitleObj(String selectedDelivery, String selectedTitle) {
        return fedoraCommunication.getTitleObj(selectedDelivery, selectedTitle);
    }

    /**
     * Get the domsItem from the uuid. The Item is fetched directly from fedora
     *
     * @param id
     * @return
     */
    public DomsItem getItemFromUuid(String id) {
        return fedoraCommunication.getItemFromUuid(id);
    }

    /**
     * Initiate TitleDeliveryHierarchy from fedora, and cash it in the model
     *
     * @throws Exception
     */
    public void initiateTitleHierachyFromFedora() throws Exception {
        currentlySelectedTitleHierarchy = fedoraCommunication.getTitleHierachyFromFedora();
    }

    /**
     * Initiate TitleDeliveryHierarchy from filesystem, and cash it in the model
     *
     * @throws Exception
     */
    public void initiateTitleHierachyFromFilesystem() throws Exception {
        currentlySelectedTitleHierarchy = filesystemReadWrite.initiateTitleHierachyFromFilesystem(currentlySelectedMonth);
    }

    /**
     * Set the month to the model, the month is used as basis of which deliveries that can currently get validated
     *
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
     *
     * @return
     *
     * @throws ParseException
     */
    public Date getSelectedMonth() throws ParseException {
        if (currentlySelectedMonth == null) {
            return new Date();
        } else {
            return dateFormat.parse(currentlySelectedMonth);
        }
    }

    /**
     * Get the currently selected month as a string
     *
     * @return
     */
    public String getSelectedMonthString() {
        return currentlySelectedMonth;
    }

    /**
     * Save the hierachy of titles and deliveries to the filesystem
     *
     * @return
     *
     * @throws Exception
     */
    public boolean saveCurrentTitleHierachyToFilesystem() throws Exception {
        return filesystemReadWrite.saveDeliveryToFilesystem(currentlySelectedMonth, currentlySelectedTitleHierarchy);
    }

    /**
     * Is the month been cashed ti the filesystem
     *
     * @return
     */
    public boolean isMonthInitiated() {
        return filesystemReadWrite.isMonthInitiated(currentlySelectedMonth);
    }

    /**
     * Get the DomsItem from the name of the delivery, the domsItem can be used for reading and writing to doms
     *
     * @param name
     * @return
     */
    public DomsItem getDeliveryFromName(String name) {
        return fedoraCommunication.getDeliveryFromName(name);
    }

    /**
     * Get a list of all deliveries, which is currently beeing operated
     *
     * @return
     */
    public Set<String> getInitiatedDeliveries() {
        return fedoraCommunication.getInitiatedDeliveries();
    }

}
