package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
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

    protected static final String[] deliveryColumns = new String[]{"chk", "initials", "title", "pages", "articles"};
    private static final String[] sectionColumns = new String[]{"sectionName", "nr", "pages"};
    private static final String[] fileColumns = new String[]{"chk", "pageName", "page", "sectionName", "section"};
    private static final String[] articleColumns = new String[]{"chk", "articleName", "page", "sectionName", "section"};

    protected DataModel model;

    protected HorizontalLayout tablesLayout = new HorizontalLayout();
    protected HorizontalLayout buttonLayout = new HorizontalLayout();

    protected GenericListTable deliveryPanel = new GenericListTable(DeliveryTitleInfo.class, "chk", null, deliveryColumns, "DELIVERY", true);
    protected GenericListTable sectionSectionTable = new GenericListTable(TitleComponent.class, null, null, sectionColumns, "SECTION", true); //
    protected GenericListTable fileSelectionPanel = new GenericListTable(Page.class, "chk", ConfirmationState.UNCHECKED, fileColumns, "PAGE", true); //
    protected GenericListTable articleSelectionPanel = new GenericListTable(Article.class, "chk", ConfirmationState.UNCHECKED, articleColumns, "ARTICLE", false);
    private Button saveCheckButton = new Button("Save check");

    /**
     * Construct the panel with a reference to the datamodel
     *
     * @param model
     */
    public DeliveryPanel(DataModel model) {
        this.model = model;
        tablesLayout.setWidth("100%");

        sectionSectionTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object selection = itemClickEvent.getItem().getItemProperty("sectionNumber").getValue();
                model.setSelectedSection(selection.toString());
                showTheSelectedTitle(false);
            }
        });
        deliveryPanel.setSortParam(deliveryColumns[2]);
        fileSelectionPanel.setSortParam("pageName");
        articleSelectionPanel.setSortParam("articleName");
    }

    /**
     * Initiate columnwith of the graphical components
     */
    @Override
    public void initialLayout() {
        tablesLayout.addComponent(deliveryPanel);
        tablesLayout.addComponent(sectionSectionTable);
        tablesLayout.addComponent(fileSelectionPanel);
        tablesLayout.addComponent(articleSelectionPanel);

        //DeliveryTitleInfo
        tablesLayout.setExpandRatio(deliveryPanel, 0.15f);
        deliveryPanel.setColumnWidth("initials",55);
        deliveryPanel.setColumnWidth("pages",35);
        deliveryPanel.setColumnWidth("articles",45);
        
        //Not visible on the DeliveryValidation Panel, only on the TitleValidationPanel
        tablesLayout.setExpandRatio(sectionSectionTable, 0.1f);
        
        
        //Page table
        tablesLayout.setExpandRatio(fileSelectionPanel, 0.4f);
        fileSelectionPanel.setColumnWidth("page",30);
        fileSelectionPanel.setColumnWidth("section",45);
        
        //Article table
        tablesLayout.setExpandRatio(articleSelectionPanel, 0.35f);
        articleSelectionPanel.setColumnWidth("page",30);
        articleSelectionPanel.setColumnWidth("section",45);
        

        saveCheckButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                viewDialogForSettingDeliveryToChecked();
            }
        });

        buttonLayout.addComponent(saveCheckButton);

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
        return fileSelectionPanel.checkSpecific(itemId, checkedState);
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

        fileSelectionPanel.setEnabled(false);
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

        fileSelectionPanel.setEnabled(true);
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
        pageList.addAll(item.getPageList());
        pageList.addAll(pages);

        List<Page> filteredPages = pageList.stream()
                .filter(p ->
                        model.getSelectedSection() == null ||
                                p.getSection().equals(model.getSelectedSection()))
                .filter(distinctByKey(page -> page.getId()))
                .collect(Collectors.toList());

        fileSelectionPanel.setInfo(filteredPages);

        List<Article> articleList = new ArrayList<Article>();
        articleList.addAll(item.getArticleList());
        articleList.addAll(articles);
        List<Article> filteredArticles = articleList.stream()
                .filter(p ->
                        model.getSelectedSection() == null ||
                                p.getSection().equals(model.getSelectedSection()))
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
        fileSelectionPanel.addItemClickListener(listener);
        articleSelectionPanel.addItemClickListener(listener);
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
        dialog.setReady(!item.isChk());
        dialog.setModal(true);

        UI.getCurrent().addWindow(dialog);
        dialog.setListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                UI.getCurrent().removeWindow(dialog);
                if ("OKBUTTON".equals(event.getButton().getId())) {

                    boolean writeResult = model.writeToCurrentItemCashed(selectedDelivery, selectedTitle, true,
                            storePanel.getInitials(), storePanel.getComment(), storePanel.getMissingItems());

                    if (!writeResult) {
                        Notification.show("The result can not get stored, please contact support", Notification.Type.ERROR_MESSAGE);
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
