package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryIdentifier;
import java.util.List;

/**
 * Created by mmj on 3/10/17.
 */
public class DeliveryInformationPanel extends DeliveryMainPanel {


    private DeliveryListPanel deliveryListPanel = new DeliveryListPanel();

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


        this.addComponent(deliveryListPanel);
        this.setExpandRatio(deliveryListPanel, 0.2f);
        super.initialLayout();
    }



    public void insertInitialTableValues()  {
        /*try {
            model.initiateTitleHierachyFromFilesystem();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        deliveryListPanel.setValues(model.getInitiatedDeliveries());
    }

    public void setDone() {
        DomsItem item = model.getDeliveryFromName(model.getSelectedDelivery());
        item.appendEvent("dashboard", new java.util.Date(), "BUGGER", "eventName", true);
    }
}
