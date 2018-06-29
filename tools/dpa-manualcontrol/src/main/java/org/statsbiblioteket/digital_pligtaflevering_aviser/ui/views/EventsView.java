package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.views;


import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.NewspaperUI;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.EventOverviewPanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.SearchPanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.StatisticsPanels;

import java.net.URI;
import java.text.ParseException;


public class EventsView extends VerticalLayout implements View {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private SearchPanel searchPanel = new SearchPanel();
    private DataModel model;
    private StatisticsPanels tabelsLayout;


    public EventsView(DataModel model, String type) {

        this.model = model;
        MenuBar header = new MenuBar();
        header.setWidth("100%");
        searchPanel.setWidth("100%");
        final VerticalLayout layout = new VerticalLayout();

        MenuBar.Command otherCommand4 = new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(NewspaperUI.EVENTOVERVIEW);
            }
        };


        header.addItem("Eventoverview", otherCommand4);

        tabelsLayout = new EventOverviewPanel(model);

        tabelsLayout.setVisible(false);

        final VerticalLayout viewLayout = new VerticalLayout();
        final HorizontalLayout viewControlLayout = new HorizontalLayout();



        tabelsLayout.setWidth("100%");
        tabelsLayout.setHeight("100%");
        layout.setMargin(true);
        addComponent(layout);

        try {
            searchPanel.setSelectedMonth(model.getSelectedMonth());
        } catch (ParseException e) {
            Notification.show("The application has hit an unexpected incedent, please contact support", Notification.Type.ERROR_MESSAGE);
            log.error("MONTH PARSER ERROR", e);
        }

        searchPanel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    if (SearchPanel.startButtonId.equals(event.getButton().getId())) {
                        model.setSelectedMonth(searchPanel.getSelectedDate());
                        if (!model.isMonthInitiated()) {
                            Notification.show("This month is not prepared", Notification.Type.ERROR_MESSAGE);
                            tabelsLayout.insertInitialTableValues();
                            panelPrepare(false);
                            return;
                        }
                        model.initiateDeliveries();
                        model.initiateTitleHierachyFromFilesystem();
                        tabelsLayout.insertInitialTableValues();
                        panelPrepare(true);
                    }

                } catch (Exception e) {
                    Notification.show("The application has hit an unexpected incedent, please contact support", Notification.Type.ERROR_MESSAGE);
                    log.error("Exception has accoured during initialization of datamodel", e);
                }

            }
        });



        layout.addComponent(header);
        layout.addComponent(searchPanel);
        viewLayout.addComponent(viewControlLayout);
        layout.addComponent(tabelsLayout);
        panelPrepare(false);
    }

    /**
     * Set panes to being prepared for viewing details
     * @param prepare
     */
    private void panelPrepare(boolean prepare) {
        tabelsLayout.setVisible(true);
    }

    /**
     * Show a welcome message when entering the page
     * If the model has been initiated with search-selections these wil be shown
     * @param event
     */
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        try {
            searchPanel.setSelectedMonth(model.getSelectedMonth());


        } catch (Exception e) {
            Notification.show("The application has hit an unexpected incedent, please contact support", Notification.Type.ERROR_MESSAGE);
            log.error("Initialization of model during entering StatisticsView has failed", e);
        }
        tabelsLayout.viewIsEntered();
        Notification.show("DPA Delivery validation");
    }
}
