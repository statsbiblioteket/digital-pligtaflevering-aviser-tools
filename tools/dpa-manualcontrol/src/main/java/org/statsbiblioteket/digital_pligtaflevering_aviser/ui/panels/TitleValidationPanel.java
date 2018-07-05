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
public class TitleValidationPanel extends DeliveryPanel {

    protected SingleStringListPanel infoPanel = new SingleStringListPanel("Title");

    public TitleValidationPanel(DataModel model) {
        super(model);
        sectionSectionTable.setVisible(true);
        deliveryPanel.setVisibleColumns(new String[]{"checked", "initials", "deliveryName", "noOfPages", "noOfArticles"});
        deliveryPanel.setSortParam("deliveryName");
        infoPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("Title").getValue();
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

        tablesLayout1.addComponent(infoPanel);
        tablesLayout1.setExpandRatio(infoPanel, 0.1f);
        super.initialLayout();
    }

    /**
     * Insert titles into first table
     * @throws Exception
     */
    @Override
    public void insertInitialTableValues() throws Exception {
        infoPanel.setTableContent(model.getTitlesFromFileSystem());
    }

    @Override
    public void viewIsEntered() {
        if (model.getSelectedTitle() != null) {
            List<DeliveryTitleInfo> list = model.getDeliverysFromTitle(model.getSelectedTitle());
            deliveryPanel.setInfo(list);
        }
        super.viewIsEntered();
    }
}
