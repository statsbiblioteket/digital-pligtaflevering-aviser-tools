package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.ConfirmationState;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.TitleComponent;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.ResultStorePanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.StoreResultWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.TitleDeliveryHierarchy.distinctByKey;

/**
 * The full panel for showing all selection details of newspaper-deliveries on the delivery format. This is the format
 * it is stored on fedora: Delivery -> Title -> Section -> Pages&articles
 */
public class DeliveryPanel extends VerticalLayout implements StatisticsPanels {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private static String[] deliveryColumns = new String[]{"checked", "initials", "newspaperTitle", "noOfPages", "noOfArticles"};
    private static String[] deliveryColumnsNames = new String[]{"chk", "initials", "newspaperTitle", "pages", "articles"};

    private static String[] sectionColumns = new String[]{"sectionName", "sectionNumber", "pageCount"};
    private static String[] sectionColumnsNames = new String[]{"sectionName", "section", "count"};

    private static String[] fileColumns = new String[]{"checkedState", "pageName", "pageNumber", "sectionName", "sectionNumber"};
    private static String[] fileColumnsNames = new String[]{"chk", "pageName", "page", "sectionName", "section"};

    private static String[] articleColumns = new String[]{"checkedState", "articleName", "pageNumber", "sectionName", "sectionNumber"};
    private static String[] articleColumnsNames = new String[]{"chk", "articleName", "page", "sectionName", "section"};

    protected DataModel model;

    protected VerticalLayout tablesLayout = new VerticalLayout();
    protected HorizontalLayout tablesLayoutTop = new HorizontalLayout();
    protected HorizontalLayout tablesLayoutBottom = new HorizontalLayout();
    protected HorizontalLayout buttonLayout = new HorizontalLayout();

    protected GenericListTable deliveryPanel = new GenericListTable(DeliveryTitleInfo.class, "checked", null, deliveryColumns, deliveryColumnsNames, "DELIVERY", true);
    protected GenericListTable sectionSectionTable = new GenericListTable(TitleComponent.class, null, null, sectionColumns, sectionColumnsNames, "SECTION", true);
    protected GenericListTable pageSelectionPanel = new GenericListTable(Page.class, "checkedState", ConfirmationState.UNCHECKED, fileColumns, fileColumnsNames, "PAGE", true);
    protected GenericListTable articleSelectionPanel = new GenericListTable(Article.class, "checkedState", ConfirmationState.UNCHECKED, articleColumns, articleColumnsNames, "ARTICLE", false);
    private Button saveCheckButton = new Button("Save check");
    private Button statusButton = new Button("StatusExport");

    /**
     * Construct the panel with a reference to the datamodel
     *
     * @param model
     */
    public DeliveryPanel(DataModel model) {
        this.model = model;
        tablesLayout.setWidth("50%");
        sectionSectionTable.setSortParam("sectionNumber");
        sectionSectionTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object selection = itemClickEvent.getItem().getItemProperty("sectionNumber").getValue();
                model.setSelectedSection(selection.toString());
                showTheSelectedTitle(false);
            }
        });
        deliveryPanel.setSortParam("newspaperTitle");
        pageSelectionPanel.setSortParam("pageName");
        articleSelectionPanel.setSortParam("articleName");
    }

    /**
     * Initiate columnwith of the graphical components
     */
    @Override
    public void initialLayout() {
        tablesLayoutTop.addComponent(deliveryPanel);
        tablesLayoutTop.addComponent(sectionSectionTable);
        tablesLayoutBottom.addComponent(pageSelectionPanel);
        tablesLayoutBottom.addComponent(articleSelectionPanel);

        tablesLayoutTop.setExpandRatio(deliveryPanel, 0.2f);
        tablesLayoutTop.setExpandRatio(sectionSectionTable, 0.2f);
        tablesLayoutBottom.setExpandRatio(pageSelectionPanel, 0.4f);
        tablesLayoutBottom.setExpandRatio(articleSelectionPanel, 0.1f);

        tablesLayout.addComponent(tablesLayoutTop);
        tablesLayout.addComponent(tablesLayoutBottom);

        saveCheckButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                viewDialogForSettingDeliveryToChecked();
            }
        });


        // second, create a StreamResource and pass the previous StreamResource:
        StreamResource resource = model.createReportResource();

        // extend a component
        FileDownloader downloader = new FileDownloader(resource);
        downloader.extend(statusButton);

        buttonLayout.addComponent(saveCheckButton);
        buttonLayout.addComponent(statusButton);

        this.addComponent(buttonLayout);
        this.addComponent(tablesLayout);
    }

    /**
     * Set the checkmark in the pageTable
     *
     * @param itemId
     * @param checkedState
     */
    @Override
    public boolean checkThePage(Object itemId, ConfirmationState checkedState) {
        return pageSelectionPanel.checkSpecific(itemId, checkedState);
    }

    public void reloadTables() {
        showTheSelectedTitle(true);
    }

    /**
     * Set the checkmark in the articleTable
     *
     * @param itemId
     * @param checkedState
     */
    @Override
    public void checkTheArticle(Object itemId, ConfirmationState checkedState) {
        articleSelectionPanel.checkSpecific(itemId, checkedState);
    }

    /**
     * Show the content of the selection defined by the delivery and title. The information is fetched from fedora as
     * the statistics stream and is shown in tables with section, article and page
     */
    protected void showTheSelectedTitle(boolean redrawSectionTable) {

        pageSelectionPanel.setEnabled(false);
        articleSelectionPanel.setEnabled(false);

        String selectedDelivery = model.getSelectedDelivery();
        String selectedTitle = model.getSelectedTitle();
        if (selectedDelivery == null || selectedTitle == null) {
            return;
        }

        Title title = model.getTitleObj(selectedDelivery, selectedTitle);
        if (title == null) {
            Notification.show("The title does not exist in the delivery", Notification.Type.ERROR_MESSAGE);
            return;
        }

        pageSelectionPanel.setEnabled(true);
        articleSelectionPanel.setEnabled(true);

        List<Page> pages = title.getPage();
        List<Article> articles = title.getArticle();

        model.selectTitleDelivery();
        DeliveryTitleInfo item = model.getCurrentDelItem();

        if (redrawSectionTable) {
            sectionSectionTable.cleanTable();
            sectionSectionTable.setInfo(UiDataConverter.sectionConverter(pages).values());
        }

        List<Page> pageList = new ArrayList<Page>();
        pageList.addAll(item.getPages());
        pageList.addAll(pages);

        List<Page> filteredPages = pageList.stream()
                .filter(p ->
                        model.getSelectedSection() == null ||
                                p.getSectionNumber().equals(model.getSelectedSection()))
                .filter(distinctByKey(page -> page.getId()))
                .collect(Collectors.toList());

        pageSelectionPanel.setInfo(filteredPages);
        pageSelectionPanel.selectFirst();

        List<Article> articleList = new ArrayList<Article>();
        articleList.addAll(item.getArticles());
        articleList.addAll(articles);
        List<Article> filteredArticles = articleList.stream()
                .filter(p ->
                        model.getSelectedSection() == null ||
                                p.getSectionNumber().equals(model.getSelectedSection()))
                .filter(distinctByKey(article -> article.getId()))
                .collect(Collectors.toList());

        articleSelectionPanel.setInfo(filteredArticles);
    }

    /**
     * Add selectionListener to fileSelectionTable
     *
     * @param listener
     */
    @Override
    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {
        pageSelectionPanel.addItemClickListener(listener);
        articleSelectionPanel.addItemClickListener(listener);
    }

    public void addValueChangeListener(Property.ValueChangeListener listener) {
        pageSelectionPanel.addValueChangeListener(listener);
    }

    @Override
    public void insertInitialTableValues() throws Exception {

    }

    /**
     * Show a dialog with the currently selected delivery and title. The delivery can then be saved as a valideted
     * delivery
     */
    @Override
    public void viewDialogForSettingDeliveryToChecked() {
        String selectedDelivery = model.getSelectedDelivery();
        String selectedTitle = model.getSelectedTitle();
        DeliveryTitleInfo item = model.getCurrentDelItem();

        final StoreResultWindow dialog = new StoreResultWindow(selectedTitle + " - " + selectedDelivery);
        ResultStorePanel storePanel = new ResultStorePanel();
        storePanel.setInitials(model.getInitials());
        dialog.setDialogContent(storePanel);
        storePanel.setValues(item);
        dialog.setReady(!item.isChecked());
        dialog.setModal(true);

        UI.getCurrent().addWindow(dialog);
        dialog.setListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                UI.getCurrent().removeWindow(dialog);
                if ("OKBUTTON".equals(event.getButton().getId())) {

                    boolean writeResult = model.writeToCurrentItemCached(selectedDelivery, selectedTitle, true,
                            storePanel.getInitials(), storePanel.getComment(), storePanel.getMissingItems(), dialog.forceClicked());

                    if (!writeResult) {
                        Notification.show("The result can not get stored, please contact support", Notification.Type.ERROR_MESSAGE);
                    } else {
                        deliveryPanel.checkSpecific(item, true);
                    }

                }
            }
        });

        dialog.addCloseListener(new Window.CloseListener() {
            //This event gets called when the dialog is closed
            @Override
            public void windowClose(Window.CloseEvent e) {
                UI.getCurrent().removeWindow(dialog);
            }
        });
    }

    @Override
    public void viewIsEntered() {
        if (model.getSelectedDelivery() != null && model.getSelectedTitle() != null) {
            try {
                insertInitialTableValues();
            } catch (Exception e) {
                Notification.show("The tables contains invalid data, please contact support", Notification.Type.ERROR_MESSAGE);
                log.error("Initialization of model during entering DeliveryPanel has failed", e);
            }
            showTheSelectedTitle(true);
        }
    }
}
