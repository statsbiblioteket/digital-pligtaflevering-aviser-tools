package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;

import java.util.List;

/**
 * Panel for validation of titles, this panel lets the user select a newspaper-title and get a list of all deliveries
 * where this title has been recieved. The user can validate one delivery at a time until all deliveries of the
 * newspaper has been approved The information is viewed on this format Delivery -> Title -> Section -> Pages&articles
 */
public class DeliveryValidationPanel extends DeliveryPanel {

    private DeliveryListPanel deliveryListPanel = new DeliveryListPanel();


    public DeliveryValidationPanel(DataModel model) {
        super(model);
        sectionSectionTable.setVisible(false);
        pageSelectionPanel.setEnabled(false);

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

        deliveryPanel.setVisibleColumns(new String[]{"checked", "initials", "newspaperTitle", "noOfPages", "noOfArticles"});
        deliveryPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {

                Object titleSelect = itemClickEvent.getItem().getItemProperty("newspaperTitle").getValue();
                model.setSelectedTitle(titleSelect.toString());

                showTheSelectedTitle(true);
            }
        });

        tablesLayoutTop.addComponent(deliveryListPanel);
        tablesLayoutTop.setExpandRatio(deliveryListPanel, 0.1f);
        super.initialLayout();

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



    @Override
    public void viewIsEntered() {
        if (model.getSelectedDelivery() != null) {
            List<DeliveryTitleInfo> l = model.getDeliveryTitleObjects();
            deliveryPanel.setInfo(l);
        }
        super.viewIsEntered();
    }
}
