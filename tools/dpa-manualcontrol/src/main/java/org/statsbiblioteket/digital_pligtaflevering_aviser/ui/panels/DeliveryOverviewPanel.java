package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.ConfirmationState;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;

import java.util.List;

/**
 * The full panel for showing all selection details of deliveries
 */
public class DeliveryOverviewPanel extends VerticalLayout implements StatisticsPanels {

    private DataModel model;
    private HorizontalLayout tablesLayout = new HorizontalLayout();
    private HorizontalLayout buttonLayout = new HorizontalLayout();
    private SingleStringListPanel infoPanel = new SingleStringListPanel();
    private DatePanel datePanel = new DatePanel();

    /**
     * Construct the panel with a reference to the datamodel
     * @param model
     */
    public DeliveryOverviewPanel(DataModel model) {
        this.model = model;

        infoPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("Item").getValue();
                String selectedTitle = page.toString();

                model.setSelectedTitle(selectedTitle);
                model.setSelectedSection(null);
                List<DeliveryTitleInfo> list = model.getDeliverysFromTitle(selectedTitle);

                datePanel.setInfo(list);
            }
        });


        tablesLayout.addComponent(infoPanel);
        tablesLayout.setExpandRatio(infoPanel, 0.2f);
        tablesLayout.addComponent(datePanel);
        tablesLayout.setExpandRatio(datePanel, 0.8f);

        this.addComponent(buttonLayout);
        this.addComponent(tablesLayout);
    }

    /**
     * Initiate columnwith of the graphical components
     */
    public void initialLayout() {
    }

    /**
     * Dummy implementation
     * @param itemId
     * @param checkedState
     */
    @Override
    public void checkThePage(Object itemId, ConfirmationState checkedState) {

    }

    /**
     * Dummy implementation
     * @param itemId
     * @param checkedState
     */
    @Override
    public void checkTheArticle(Object itemId, ConfirmationState checkedState) {

    }

    /**
     * Dummy implementation
     * @param listener
     */
    @Override
    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {

    }


    public void insertInitialTableValues() throws Exception {
        infoPanel.setTableContent(model.getTitlesFromFileSystem());
    }

    /**
     * Dummy implementation
     */
    @Override
    public void viewDialogForSettingDeliveryToChecked() {
    }
}
