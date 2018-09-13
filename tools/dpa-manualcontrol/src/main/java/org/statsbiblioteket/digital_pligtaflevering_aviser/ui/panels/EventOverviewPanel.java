package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.ConfirmationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.Constants;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryInformationComponent;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.EventDTO;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.Settings;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.EventAdminWindow;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.EventPanel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

                //Her henter vi events datastreamen med de store stack traces
                List<dk.statsbiblioteket.medieplatform.autonomous.Event> eventList = domsItem.getOriginalEvents();
                EventAdminWindow dialog = new EventAdminWindow(clickEvent.getButton().getId(), true);
                EventPanel eventPanel = new EventPanel();

                eventPanel.addButtonEventListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {

                        Optional<DomsDatastream> validationStream = Optional.empty();
                        switch(clickEvent.getButton().getId()) {
                            case Constants.VERA_PDF_ANALYZED_EVENT:
                                validationStream = domsItem.datastreams().stream().filter(validationStreams -> validationStreams.getId().equals(
                                        Constants.DS_VERAPDFREPORT)).findAny();
                                break;
                            case Constants.WEEKDAYS_ANALYZED_EVENT:
                                validationStream = domsItem.datastreams().stream().filter(validationStreams -> validationStreams.getId().equals(
                                        Constants.DS_NEWSPAPERWEEKDAY)).findAny();
                                break;
                            case Constants.STATISTICS_GENERATED_EVENT:
                                validationStream = domsItem.datastreams().stream().filter(validationStreams -> validationStreams.getId().equals(
                                        Constants.DS_DELIVERYSTATISTICS)).findAny();
                                break;
                        }

                        if (validationStream.isPresent()) {
                            String validationString = validationStream.get().getDatastreamAsString();

                            TextArea field = new TextArea();
                            field.setWidth(1000, Unit.PIXELS);
                            field.setRows(50);
                            field.setValue(validationString);

                            //Open a dialog with the simple content of a textarea containing the stream from the event-log
                            EventAdminWindow textDialog = new EventAdminWindow(clickEvent.getButton().getId(), false);
                            textDialog.setDialogContent(field);
                            textDialog.setModal(true);

                            textDialog.addCloseListener(new Window.CloseListener() {
                                // inline close-listener
                                @Override
                                public void windowClose(Window.CloseEvent e) {
                                    UI.getCurrent().removeWindow(textDialog);
                                }
                            });
                            UI.getCurrent().addWindow(textDialog);
                        }
                    }
                });

                eventPanel.setValues(UiDataConverter.convertList(eventList));
                eventPanel.setInitials(model.getInitials());

                dialog.setDialogContent(eventPanel);
                dialog.setModal(true);

                UI.getCurrent().addWindow(dialog);
                dialog.setListener(new Button.ClickListener() {
                    //These buttons are defined in EventAdminWindow
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        UI.getCurrent().removeWindow(dialog);

                        if(Arrays.stream(Settings.trustedUsers)
                                 .noneMatch(initials -> initials.equals(model.getInitials()))) {
                            Notification.show("You are not added to the list of trusted users ("+Arrays.toString(Settings.trustedUsers)+")",
                                              Notification.Type.HUMANIZED_MESSAGE);
                            return;
                        }
                        EventDTO selectedDomsEvent = (EventDTO) eventPanel.getSelection();
                        DomsEvent newEvent = null;
                        switch (event.getButton().getId()) {
                            case EventAdminWindow.OVERRIDE_BUTTON:
                                //First we break out if you try to override an event that are already succesful
                                if (selectedDomsEvent.isSuccess()){
                                    Notification.show("You can only override events that have failed ("+selectedDomsEvent.toString()+")",
                                                      Notification.Type.HUMANIZED_MESSAGE);
                                    return;
                                }
                                
                                //Then we add an event to show that we have overridden something. This can be used by the GUI to show such modified deliveries
                                newEvent = new DomsEvent(Constants.AGENT_IDENTIFIER_VALUE,
                                                         new java.util.Date(),
                                                         model.getInitials()+" have overridden "+selectedDomsEvent.getEventID(),
                                                         Constants.OVERRIDE_EVENT,
                                                         true);
                                domsItem.appendEvent(newEvent);
        
                                //Then we add a new event with the overridden ID and a higher date, now with outcome success
                                newEvent = new DomsEvent(Constants.AGENT_IDENTIFIER_VALUE,
                                                         new java.util.Date(),
                                                         "override by " + model.getInitials(),
                                                         selectedDomsEvent.getEventID(),
                                                         true);
                                break;
        
                            case EventAdminWindow.DELETE_BUTTON:
                                int noOfEvents = domsItem.removeEvents(selectedDomsEvent.getEventID());
                                String note = "Deleted " + noOfEvents + " instances of "
                                              + selectedDomsEvent.getEventID() +
                                              (selectedDomsEvent.getDetails() == null ? "" :
                                                       "\n" +
                                                       "\nReason: "
                                                       + selectedDomsEvent.getEventID()
                                                       + "\nBy: " + model.getInitials());
                                newEvent = new DomsEvent(Constants.AGENT_IDENTIFIER_VALUE,
                                                         new java.util.Date(),
                                                         note,
                                                         Constants.EVENT_DELETED_EVENT,
                                                         true);
                                break;
        
                            case EventAdminWindow.STOP_BUTTON:
                                newEvent = new DomsEvent(Constants.AGENT_IDENTIFIER_VALUE,
                                                         new java.util.Date(),
                                                         "Adding an Event to force a roundtrip to be manually stopped",
                                                         Constants.STOPPED_EVENT,
                                                         true);
                                break;
        
                            case EventAdminWindow.APPROVE_BUTTON:
                                newEvent = new DomsEvent(Constants.AGENT_IDENTIFIER_VALUE,
                                                         new java.util.Date(),
                                                         "Approving Roundtrip by: "
                                                         + model.getInitials(),
                                                         Constants.APPROVED_EVENT,
                                                         true);
                                break;
        
                        }
                        if (newEvent != null) {
                            domsItem.appendEvent(newEvent);
        
                        }
                        
                        
                    //    TODO reload view here
                    }
                });

                dialog.addCloseListener(new Window.CloseListener() {
                    // inline close-listener
                    @Override
                    public void windowClose(Window.CloseEvent e) {
                        UI.getCurrent().removeWindow(dialog);
                    }
                });
            }
        };

        datePanel.addClickListener(buttonListener);
        datePanel.setWidth("100%");
        tablesLayout.addComponent(datePanel);
        tablesLayout.setWidth("100%");
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

        try {
            datePanel.setMonth(model.getSelectedMonth());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<DeliveryInformationComponent> deliveryInformationList = new ArrayList<DeliveryInformationComponent>();
        for(String item : model.getInitiatedDeliveries()) {
            DomsItem domsItem = model.getDeliveryFromName(item);
            List<dk.statsbiblioteket.medieplatform.autonomous.Event> originalEvents = domsItem.getOriginalEvents();
            DeliveryInformationComponent.ValidationState validationState = UiDataConverter.validateEventCollection(
                    originalEvents);
            boolean overridden = UiDataConverter.isEventOverridden(originalEvents);
            
            DeliveryInformationComponent deliveryInformationComponent = new DeliveryInformationComponent(item, validationState, overridden);
            
            deliveryInformationList.add(deliveryInformationComponent);
        }
        datePanel.setInfo(deliveryInformationList);
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
