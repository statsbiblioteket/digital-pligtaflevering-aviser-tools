package org.kb.ui.panels;

import com.vaadin.event.ItemClickEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import org.kb.ui.FetchEventStructure;
import org.kb.ui.datamodel.DataModel;
import org.kb.ui.datamodel.FileComponent;

import java.util.ArrayList;


/**
 * Created by mmj on 3/10/17.
 */
public class DeliveryInformationPanel2 extends DeliveryMainPanel {


    private DataModel model;
    private SingleStringListPanel infoPanel = new SingleStringListPanel();
    private SingleStringListPanel deliveryPanel = new SingleStringListPanel();
    private FileListTable fileSelectionPanel = new FileListTable(Page.class);
    private ArrayList<FileComponent> alr = new ArrayList<FileComponent>();


    public DeliveryInformationPanel2(DataModel model) {
        this.setWidth("100%");


        infoPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("Batch").getValue();
                model.setSelectedDelivery(page.toString());
                showTheSelectedPage();
            }
        });


        deliveryPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("Batch").getValue();
                model.setSelectedTitle(page.toString());
                showTheSelectedPage();
            }
        });




        this.model = model;
        this.addComponent(infoPanel);
        this.addComponent(deliveryPanel);
        //this.addComponent(table1);
        this.addComponent(fileSelectionPanel);
    }

    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {
        fileSelectionPanel.addItemClickListener(listener);
    }

    private void showTheSelectedPage() {

        System.out.println(model.getSelectedDelivery());
        System.out.println(model.getSelectedTitle());

        //fileSelectionPanel.

    }


    public void performInitialSearch(FetchEventStructure eventStructureCommunication, String info) {
        deliveryPanel.setInfo(eventStructureCommunication, "Data_Archived");
        infoPanel.setTheStuff(model.getTitles());
    }
}
