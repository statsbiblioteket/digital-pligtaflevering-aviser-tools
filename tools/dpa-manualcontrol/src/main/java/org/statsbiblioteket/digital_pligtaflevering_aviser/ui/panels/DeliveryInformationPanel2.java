package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryIdentifier;

import java.util.List;



/**
 * Created by mmj on 3/10/17.
 */
public class DeliveryInformationPanel2 extends DeliveryMainPanel {

    protected SingleStringListPanel infoPanel = new SingleStringListPanel();

    public DeliveryInformationPanel2(DataModel model) {
        super(model);
        deliveryPanel.setVisibleColumns(new String[]{"checked", "initials", "deliveryName", "noOfArticles", "noOfPages"});
        infoPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("Item").getValue();
                String selectedTitle = page.toString();

                model.setSelectedTitle(selectedTitle);
                model.setSelectedSection(null);
                showTheSelectedTitle();
                List<DeliveryIdentifier> list = model.getDeliverysFromTitle(selectedTitle);

                deliveryPanel.setInfo(list);
            }
        });

        deliveryPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("deliveryName").getValue();
                model.setSelectedDelivery(page.toString());
                model.setSelectedSection(null);
                showTheSelectedTitle();

            }
        });

        this.addComponent(infoPanel);
        this.setExpandRatio(infoPanel, 0.1f);
        super.initialLayout();
    }

    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {
        fileSelectionPanel.addItemClickListener(listener);
        articleSelectionPanel.addItemClickListener(listener);
    }

    public void insertInitialTableValues() throws Exception {
        infoPanel.setTableContent(model.getTitlesFromFileSystem());
    }


    public void setDone() {
        //item.appendEvent("dashboard", new java.util.Date(), message == null ? "" : message, eventName, outcome);
    }
}
