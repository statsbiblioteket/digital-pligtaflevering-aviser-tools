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

                        Optional<DomsDatastream> validationStream = null;
                        switch(clickEvent.getButton().getId()) {
                            case "VeraPDF_Analyzed":
                                validationStream = domsItem.datastreams().stream().filter(validationStreams -> validationStreams.getId().equals("VERAPDFREPORT")).findAny();
                                break;
                            case "Newspaper_Weekdays_Analyzed":
                                validationStream = domsItem.datastreams().stream().filter(validationStreams -> validationStreams.getId().equals("NEWSPAPERWEEKDAY")).findAny();
                                break;
                            case "Statistics_generated":
                                validationStream = domsItem.datastreams().stream().filter(validationStreams -> validationStreams.getId().equals("DELIVERYSTATISTICS")).findAny();
                                break;
                        }

                        if (validationStream!=null && validationStream.isPresent()) {
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
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        UI.getCurrent().removeWindow(dialog);

                        if(Arrays.stream(Settings.trustedUsers).filter(initials -> initials.equals(model.getInitials())).count()>0) {
                            if ("OVERRIDE".equals(event.getButton().getId())) {
                                EventDTO selectedDomsEvent = (EventDTO) eventPanel.getSelection();
                                DomsEvent overrideDomsEvent = new DomsEvent("manualcontrol", new java.util.Date(),
                                        "override by " + model.getInitials(), selectedDomsEvent.getEventID(), true);
                                domsItem.appendEvent(overrideDomsEvent);
                            } else if ("DELETE".equals(event.getButton().getId())) {
                                EventDTO selectedDomsEvent = (EventDTO) eventPanel.getSelection();
                                int noOfEvents = domsItem.removeEvents(selectedDomsEvent.getEventID());
                                DomsEvent newDeleteDomsEvent = new DomsEvent("manualcontrol", new java.util.Date(),
                                        "Deleted " + noOfEvents + " instances of " + selectedDomsEvent.getEventID() +
                                                (selectedDomsEvent.getDetails() == null ? "" : "\n" +
                                                        "\nReason: " + selectedDomsEvent.getEventID()+ "\nBy: "+model.getInitials()), "Event_deleted_manually", true);
                                domsItem.appendEvent(newDeleteDomsEvent);
                            } else if ("STOP".equals(event.getButton().getId())) {
                                DomsEvent newDeleteDomsEvent = new DomsEvent("manualcontrol", new java.util.Date(),
                                        "Adding an Event to force a roundtrip to be manually stopped", "Manually_stopped", true);
                                domsItem.appendEvent(newDeleteDomsEvent);
                            }
                        } else {
                            Notification.show("You are not added to the list of trusted users", Notification.Type.HUMANIZED_MESSAGE);
                        }
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
            deliveryInformationList.add(new DeliveryInformationComponent(item, UiDataConverter.validateEventCollection(domsItem.getOriginalEvents())));
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
