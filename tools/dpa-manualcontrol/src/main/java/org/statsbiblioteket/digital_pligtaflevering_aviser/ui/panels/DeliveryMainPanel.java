package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryIdentifier;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.TitleComponent;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.ResultStorePanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.StoreResultWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mmj on 3/10/17.
 */
public class DeliveryMainPanel extends VerticalLayout {

    protected DataModel model;//

    protected HorizontalLayout tablesLayout = new HorizontalLayout();
    protected HorizontalLayout buttonLayout = new HorizontalLayout();

    protected GenericListTable deliveryPanel = new GenericListTable(DeliveryIdentifier.class, "checked", new String[]{"checked", "initials", "newspaperTitle", "noOfArticles", "noOfPages"}, "DELIVERY");
    protected GenericListTable sectionSectionTable = new GenericListTable(TitleComponent.class, null, new String[]{"sectionName", "sectionNumber"}, "SECTION");//
    protected GenericListTable fileSelectionPanel = new GenericListTable(Page.class, null, new String[]{"pageName", "pageNumber", "sectionName", "sectionNumber"}, "PAGE");//
    protected GenericListTable articleSelectionPanel = new GenericListTable(Article.class, null, null, "ARTICLE");
    private Button saveCheckButton = new Button("Save check");



    public DeliveryMainPanel() {
        this.setWidth("100%");
        this.setHeight("100%");
    }


    public DeliveryMainPanel(DataModel model) {
        this.model = model;
        tablesLayout.setWidth("100%");


        sectionSectionTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object selection = itemClickEvent.getItem().getItemProperty("sectionNumber").getValue();
                model.setSelectedSection(selection.toString());
                showTheSelectedTitle();
            }
        });
    }

    public void initialLayout() {
        tablesLayout.addComponent(deliveryPanel);
        tablesLayout.addComponent(sectionSectionTable);
        tablesLayout.addComponent(fileSelectionPanel);
        tablesLayout.addComponent(articleSelectionPanel);

        tablesLayout.setExpandRatio(deliveryPanel, 0.2f);
        tablesLayout.setExpandRatio(sectionSectionTable, 0.2f);
        tablesLayout.setExpandRatio(fileSelectionPanel, 0.4f);
        tablesLayout.setExpandRatio(articleSelectionPanel, 0.1f);

        saveCheckButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                setCheckedState();
            }});

        buttonLayout.addComponent(saveCheckButton);

        this.addComponent(buttonLayout);
        this.addComponent(tablesLayout);
    }


    protected void showTheSelectedTitle() {

        fileSelectionPanel.setEnabled(false);
        articleSelectionPanel.setEnabled(false);

        String selectedDelivery = model.getSelectedDelivery();
        String selectedTitle = model.getSelectedTitle();
        if(selectedDelivery==null || selectedTitle==null) {
            return;
        }

        Title title = model.getTitleObj(selectedDelivery, selectedTitle);
        if(title==null) {
            return;
        }

        fileSelectionPanel.setEnabled(true);
        articleSelectionPanel.setEnabled(true);

        List<Page> pages = title.getPage();
        List<Article> articles = title.getArticle();
        articleSelectionPanel.setInfo(articles);

        if(model.getSelectedSection() != null) {
            List<Page> filteredPages = new ArrayList<Page>();
            for(Page page : pages) {
                if(model.getSelectedSection().equals(page.getSectionNumber())) {
                    filteredPages.add(page);
                }
            }
            fileSelectionPanel.setInfo(filteredPages);
        } else {
            sectionSectionTable.cleanTable();
            sectionSectionTable.setInfo(UiDataConverter.sectionConverter(title.getPage().iterator(), null).values());
            fileSelectionPanel.setInfo(pages);
        }
        model.selectTitleDelivery();
    }


    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {
        fileSelectionPanel.addItemClickListener(listener);
    }

    public void insertInitialTableValues() throws Exception {

    }

    public void setCheckedState() {
        String selectedDelivery = model.getSelectedDelivery();
        String selectedTitle = model.getSelectedTitle();
        DeliveryIdentifier item = model.getCurrentDelItem();

        final StoreResultWindow dialog = new StoreResultWindow(selectedTitle + " - " + selectedDelivery);
        ResultStorePanel storePanel = new ResultStorePanel();

        dialog.setDialogContent(storePanel);
        storePanel.setValues(item);
        dialog.setReady(!item.isChecked());
        dialog.setModal(true);

        UI.getCurrent().addWindow(dialog);
        dialog.setListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                UI.getCurrent().removeWindow(dialog);
                if("OKBUTTON".equals(event.getButton().getId())) {
                    model.writeToCurrentItemCashed(selectedDelivery, selectedTitle, true, storePanel.getInitials(), storePanel.getComment());
                }
            }});

        dialog.addCloseListener(new Window.CloseListener() {
            // inline close-listener
            public void windowClose(Window.CloseEvent e) {

                UI.getCurrent().removeWindow(dialog);
            }
        });
    }
}
