package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;

import java.util.List;



/**
 * Panel for validation of titles, this panel lets the user select a newspaper-title and get a list of all deliveries where this title has been recieved.
 * The user can validate one delivery at a time until all deliveries of the newspaper has been approved
 * The information is viewed on this format
 * Title -> Delivery -> Section -> Pages&articles
 */
public class TitleValidationPanel extends DeliveryMainPanel {

    protected SingleStringListPanel infoPanel = new SingleStringListPanel();

    public TitleValidationPanel(DataModel model) {
        super(model);
        sectionSectionTable.setVisible(true);
        deliveryPanel.setVisibleColumns(new String[]{"checked", "initials", "deliveryName", "noOfArticles", "noOfPages"});
        infoPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("Item").getValue();
                String selectedTitle = page.toString();

                model.setSelectedTitle(selectedTitle);
                model.setSelectedSection(null);
                showTheSelectedTitle(true);
                List<DeliveryTitleInfo> list = model.getDeliverysFromTitle(selectedTitle);

                deliveryPanel.setInfo(list);
            }
        });

        deliveryPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("deliveryName").getValue();
                model.setSelectedDelivery(page.toString());
                model.setSelectedSection(null);
                showTheSelectedTitle(true);

            }
        });

        tablesLayout.addComponent(infoPanel);
        tablesLayout.setExpandRatio(infoPanel, 0.1f);
        super.initialLayout();
    }

    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {
        fileSelectionPanel.addItemClickListener(listener);
        articleSelectionPanel.addItemClickListener(listener);
    }

    public void insertInitialTableValues() throws Exception {
        infoPanel.setTableContent(model.getTitlesFromFileSystem());
    }
}
