package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.NewspaperContextListener;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.DeliveryConfirmPanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.StoreResultWindow;

import java.util.List;

/**
 * Panel for validation of titles, this panel lets the user select a newspaper-title and get a list of all deliveries
 * where this title has been recieved. The user can validate one delivery at a time until all deliveries of the
 * newspaper has been approved The information is viewed on this format Delivery -> Title -> Section -> Pages&articles
 */
public class DeliveryValidationPanel extends DeliveryPanel {

    private DeliveryListPanel deliveryListPanel = new DeliveryListPanel();
    private Button doneDeliveryButton = new Button("Done del");

    public DeliveryValidationPanel(DataModel model) {
        super(model);
        sectionSectionTable.setVisible(false);
        fileSelectionPanel.setEnabled(false);

        deliveryListPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                com.vaadin.data.Item selectedItem = itemClickEvent.getItem();
                String selectedDelivery = selectedItem.getItemProperty("Name").getValue().toString();
                model.setSelectedDelivery(selectedDelivery);
                List<DeliveryTitleInfo> l = model.getDeliveryTitleObjects();
                deliveryPanel.setInfo(l);
            }
        });

        deliveryPanel.setVisibleColumns(deliveryColumns);
        deliveryPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {

                Object titleSelect = itemClickEvent.getItem().getItemProperty("title").getValue();
                model.setSelectedTitle(titleSelect.toString());

                showTheSelectedTitle(true);
            }
        });

        tablesLayout.addComponent(deliveryListPanel);
        tablesLayout.setExpandRatio(deliveryListPanel, 0.1f);
        super.initialLayout();

        doneDeliveryButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                setDone();
            }
        });
        buttonLayout.addComponent(doneDeliveryButton);
    }

    /**
     * Insert deliveries into first table
     *
     * @throws Exception
     */
    @Override
    public void insertInitialTableValues() {
        deliveryListPanel.setValues(model.getInitiatedDeliveries());
    }

    /**
     * Show the dialog for adding the event "manualDeliveryValidation" to a delivery
     */
    public void setDone() {

        if (!deliveryPanel.isAllChecked()) {
            Notification.show("The delivery can not be confirmed until all titles is confirmed");
            return;
        }

        String selectedDelivery = model.getSelectedDelivery();
        final StoreResultWindow dialog = new StoreResultWindow(selectedDelivery);
        DeliveryConfirmPanel storePanel = new DeliveryConfirmPanel();

        dialog.setDialogContent(storePanel);
        dialog.setModal(true);

        UI.getCurrent().addWindow(dialog);
        dialog.setListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                UI.getCurrent().removeWindow(dialog);
                if ("OKBUTTON".equals(event.getButton().getId())) {
                    DomsItem item = model.getDeliveryFromName(model.getSelectedDelivery());
                    item.appendEvent(new DomsEvent(NewspaperContextListener.manualCheckEventname, new java.util.Date(), "Validation of manual delivery", NewspaperContextListener.manualCheckEventname, true));
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

    @Override
    public void viewIsEntered() {
        if (model.getSelectedDelivery() != null) {
            List<DeliveryTitleInfo> l = model.getDeliveryTitleObjects();
            deliveryPanel.setInfo(l);
        }
        super.viewIsEntered();
    }
}
