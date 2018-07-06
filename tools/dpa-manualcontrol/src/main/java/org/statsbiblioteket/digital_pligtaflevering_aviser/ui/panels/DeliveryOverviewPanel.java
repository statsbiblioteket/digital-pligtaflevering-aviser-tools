package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.ConfirmationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;

import java.text.ParseException;
import java.util.List;

/**
 * The Calenderview
 * The full panel for showing all selection details of deliveries
 */
public class DeliveryOverviewPanel extends VerticalLayout implements StatisticsPanels {

    private Logger log = LoggerFactory.getLogger(getClass());

    private DataModel model;
    private HorizontalLayout tablesLayout = new HorizontalLayout();
    private HorizontalLayout buttonLayout = new HorizontalLayout();
    private SingleStringListPanel infoPanel = new SingleStringListPanel("Title");
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
                Object page = itemClickEvent.getItem().getItemProperty("Title").getValue();
                String selectedTitle = page.toString();

                model.setSelectedTitle(selectedTitle);
                model.setSelectedSection(null);
                List<DeliveryTitleInfo> list = model.getDeliverysFromTitle(selectedTitle);

                try {
                    datePanel.setMonth(model.getSelectedMonth());
                } catch (ParseException e) {
                    log.error("Date could not get parsed", e);
                }
                datePanel.setInfo(list, model.getDeliveryPattern().getDeliveryPattern(selectedTitle));
            }
        });


        tablesLayout.addComponent(infoPanel);
        infoPanel.setWidth("200px");
        tablesLayout.addComponent(datePanel);
        tablesLayout.setExpandRatio(datePanel, 1f);

        this.addComponent(buttonLayout);
        this.addComponent(tablesLayout);
    }

    /**
     * Initiate columnwith of the graphical components
     */
    @Override
    public void initialLayout() {
    }

    /**
     * Dummy implementation
     * @param itemId
     * @param checkedState
     */
    @Override
    public boolean checkThePage(Object itemId, ConfirmationState checkedState) {
        return false;
    }

    /**
     * Dummy implementation
     * @param itemId
     * @param checkedState
     */
    @Override
    public void checkTheArticle(Object itemId, ConfirmationState checkedState) {

    }

    @Override
    public void reloadTables() {

    }

    /**
     * Dummy implementation
     * @param listener
     */
    @Override
    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {

    }

    @Override
    public void addValueChangeListener(Property.ValueChangeListener listener) {

    }


    @Override
    public void insertInitialTableValues() throws Exception {
        infoPanel.setTableContent(model.getTitlesFromFileSystem());
    }

    /**
     * Dummy implementation
     */
    @Override
    public void viewDialogForSettingDeliveryToChecked() {
    }

    /**
     * Dummy implementation
     */
    @Override
    public void viewIsEntered() {

    }
}
