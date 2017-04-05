package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryIdentifier;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by mmj on 3/10/17.
 */
public class DeliveryInformationPanel extends DeliveryMainPanel {


    private DeliveryListPanel deliveryListPanel = new DeliveryListPanel();
    private GenericListTable titleListPanel = new GenericListTable(DeliveryIdentifier.class, "checked", new String[]{"checked", "initials", "newspaperTitle", "noOfArticles", "noOfPages"}, "DELIVERY");

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
                titleListPanel.setInfo(l);
            }
        });






        titleListPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {

                Object titleSelect = itemClickEvent.getItem().getItemProperty("newspaperTitle").getValue();
                model.setSelectedTitle(titleSelect.toString());

                String selectedDelivery = model.getSelectedDelivery();
                String selectedTitle = model.getSelectedTitle();

                if(selectedDelivery==null || selectedTitle==null) {
                    return;
                }

                Title title = model.getTitleObj(selectedDelivery, selectedTitle);
                if(title==null) {
                    return;
                }


                List<Page> pages = title.getPage();
                sectionSectionTable.setInfo(UiDataConverter.sectionConverter(pages.iterator(), null).values());
                fileSelectionPanel.setEnabled(true);

                if(selectedSection != null) {
                    List<Page> filteredPages = new ArrayList<Page>();
                    for(Page page : pages) {
                        if(selectedSection.equals(page.getSectionNumber())) {
                            filteredPages.add(page);
                        }
                    }
                    fileSelectionPanel.setInfo(filteredPages);
                } else {
                    fileSelectionPanel.setInfo(pages);
                }
            }
        });


        this.addComponent(deliveryListPanel);
        this.addComponent(titleListPanel);
        this.addComponent(sectionSectionTable);
        this.addComponent(fileSelectionPanel);


        this.setExpandRatio(deliveryListPanel, 0.2f);
        this.setExpandRatio(titleListPanel, 0.2f);
        this.setExpandRatio(sectionSectionTable, 0.2f);
        this.setExpandRatio(fileSelectionPanel, 0.4f);

    }



    public void performIt()  {
        try {
            model.initiateTitleHierachyFromFilesystem();
        } catch (Exception e) {
            e.printStackTrace();
        }
        deliveryListPanel.setTheStuff(model.getInitiatedDeliveries());
    }
}
