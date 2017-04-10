package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryIdentifier;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.DeliveryValidationPanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.ResultStorePanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.StoreResultWindow;

import java.util.Iterator;
import java.util.List;

/**
 * Created by mmj on 3/10/17.
 */
public class DeliveryInformationPanel extends DeliveryMainPanel {


    private DeliveryListPanel deliveryListPanel = new DeliveryListPanel();
    private Button doneDeliveryButton = new Button("Done del");

    public DeliveryInformationPanel(DataModel model) {
        super(model);
        fileSelectionPanel.setEnabled(false);

        deliveryListPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                com.vaadin.data.Item selectedItem = itemClickEvent.getItem();
                String selectedDelivery = selectedItem.getItemProperty("Name").getValue().toString();
                model.setSelectedDelivery(selectedDelivery);
                List<DeliveryIdentifier> l = model.getOtherFromDelivery();
                deliveryPanel.setInfo(l);
            }
        });


        deliveryPanel.setVisibleColumns(new String[]{"checked", "initials", "newspaperTitle", "noOfArticles", "noOfPages"});
        deliveryPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {

                Object titleSelect = itemClickEvent.getItem().getItemProperty("newspaperTitle").getValue();
                model.setSelectedTitle(titleSelect.toString());

                showTheSelectedTitle();
            }
        });


        tablesLayout.addComponent(deliveryListPanel);
        tablesLayout.setExpandRatio(deliveryListPanel, 0.2f);
        super.initialLayout();

        doneDeliveryButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                setDone();
            }});
        buttonLayout.addComponent(doneDeliveryButton);
    }



    public void insertInitialTableValues()  {
        deliveryListPanel.setValues(model.getInitiatedDeliveries());
    }

    public void setDone() {

        if(!deliveryPanel.isAllChecked()) {
            Notification.show("The delivery can not be confirmed until all titles is confirmed");
            return;
        }

        String selectedDelivery = model.getSelectedDelivery();
        final StoreResultWindow dialog = new StoreResultWindow(selectedDelivery);
        DeliveryValidationPanel storePanel = new DeliveryValidationPanel();

        dialog.setDialogContent(storePanel);
        dialog.setModal(true);

        UI.getCurrent().addWindow(dialog);
        dialog.setListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                UI.getCurrent().removeWindow(dialog);
                if("OKBUTTON".equals(event.getButton().getId())) {
                    DomsItem item = model.getDeliveryFromName(model.getSelectedDelivery());
                    item.appendEvent("manualDeliveryValidation", new java.util.Date(), "Validation of manual delivery", "ManualValidationDone", true);
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
