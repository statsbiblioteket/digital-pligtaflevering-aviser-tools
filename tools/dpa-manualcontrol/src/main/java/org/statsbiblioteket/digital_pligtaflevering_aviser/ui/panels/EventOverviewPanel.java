package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.ConfirmationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.EventPanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.StoreResultWindow;

import java.text.ParseException;
import java.util.List;

/**
 * The EventOverviewPanel
 * The full panel for showing all selection details of deliveries
 */
public class EventOverviewPanel extends VerticalLayout implements StatisticsPanels {

    private Logger log = LoggerFactory.getLogger(getClass());

    private DataModel model;
    private HorizontalLayout tablesLayout = new HorizontalLayout();
    private HorizontalLayout buttonLayout = new HorizontalLayout();
    private EventDatePanel datePanel = new EventDatePanel();

    /**
     * Construct the panel with a reference to the datamodel
     * @param model
     */
    public EventOverviewPanel(DataModel model) {
        this.model = model;

        Button.ClickListener buttonListener = new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                DomsItem domsItem = model.getDeliveryFromName(clickEvent.getButton().getId());

                List<dk.statsbiblioteket.medieplatform.autonomous.Event> eventList = domsItem.getOriginalEvents();

                StoreResultWindow dialog = new StoreResultWindow(clickEvent.getButton().getId());

                EventPanel eventPanel = new EventPanel();
                eventPanel.setValues(eventList);
                eventPanel.setInitials(model.getInitials());

                dialog.setDialogContent(eventPanel);
                dialog.setModal(true);

                UI.getCurrent().addWindow(dialog);
                dialog.setListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        UI.getCurrent().removeWindow(dialog);
                        if ("OKBUTTON".equals(event.getButton().getId())) {

                            dk.statsbiblioteket.medieplatform.autonomous.Event oo = (dk.statsbiblioteket.medieplatform.autonomous.Event)eventPanel.getSelection();
                            domsItem.removeEvents(oo.getEventID());
                            UI.getCurrent().removeWindow(dialog);
                        }
                    }
                });
            }
        };

        datePanel.addClickListener(buttonListener);

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
    public void insertInitialTableValues() throws Exception {


        try {
            datePanel.setMonth(model.getSelectedMonth());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        datePanel.setInfo(model.getInitiatedDeliveries());
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
