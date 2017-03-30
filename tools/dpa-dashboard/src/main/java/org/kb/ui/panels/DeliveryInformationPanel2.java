package org.kb.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.kb.ui.datamodel.DataModel;
import org.kb.ui.datamodel.DeliveryIdentifier;
import org.kb.ui.datamodel.UiDataConverter;
import org.kb.ui.windows.ResultStorePanel;
import org.kb.ui.windows.StoreResultWindow;

import java.util.List;



/**
 * Created by mmj on 3/10/17.
 */
public class DeliveryInformationPanel2 extends DeliveryMainPanel {


    protected SingleStringListPanel infoPanel = new SingleStringListPanel();
    protected GenericListTable deliveryPanel = new GenericListTable(DeliveryIdentifier.class, "checked");


    public DeliveryInformationPanel2(DataModel model) {
        super(model);
        infoPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("Item").getValue();
                String selectedTitle = page.toString();

                model.setSelectedTitle(selectedTitle);


                /*try {
                    model.initiateTitleHierachyFromFedora();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/

                showTheSelectedTitle();
                List<DeliveryIdentifier> list = model.getDeliverysFromTitle(selectedTitle);

                deliveryPanel.setInfo(list);


            }
        });


        deliveryPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("name").getValue();
                model.setSelectedDelivery(page.toString());


                model.selectTitleDelivery();
                /*try {
                    model.initiateTitleHierachyFromFedora();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/





                showTheSelectedTitle();
            }
        });


        this.addComponent(infoPanel);
        this.addComponent(deliveryPanel);
        this.addComponent(sectionSectionTable);
        this.addComponent(fileSelectionPanel);
    }

    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {
        fileSelectionPanel.addItemClickListener(listener);
    }

    private void showTheSelectedTitle() {

        sectionSectionTable.cleanTable();
        fileSelectionPanel.setEnabled(false);

        String selectedDelivery = model.getSelectedDelivery();
        String selectedTitle = model.getSelectedTitle();
        if(selectedDelivery==null || selectedTitle==null) {
            return;
        }

        Title it = model.getTitleObj(selectedDelivery, selectedTitle);
        if(it==null) {
            return;
        }
        sectionSectionTable.setInfo(UiDataConverter.sectionConverter(it.getPage().iterator()).values());
        fileSelectionPanel.setEnabled(true);
        fileSelectionPanel.setInfo(it.getPage());
    }

    public List runThrough() {

        List<DeliveryIdentifier> deliveryList = deliveryPanel.getSelections();

        for(DeliveryIdentifier deliveryId : deliveryList) {
            if(deliveryId.isChecked()) {

                String selectedDelivery = model.getSelectedDelivery();
                String selectedTitle = model.getSelectedTitle();

                DeliveryIdentifier item = model.getCurrentDelItem();


                final StoreResultWindow dialog = new StoreResultWindow(selectedTitle + " - " + selectedDelivery);
                ResultStorePanel storePanel = new ResultStorePanel();
                storePanel.setValues(item);


                dialog.setDialogContent(storePanel);
                dialog.setModal(true);


                UI.getCurrent().addWindow(dialog);
                dialog.setListener(new Button.ClickListener() {
                    public void buttonClick(Button.ClickEvent event) {

                        UI.getCurrent().removeWindow(dialog);


                        model.writeToCurrentItemCashed(selectedDelivery, selectedTitle, true, storePanel.getInitials(), storePanel.getComment());

                    }});



                dialog.addCloseListener(new Window.CloseListener() {
                    // inline close-listener
                    public void windowClose(Window.CloseEvent e) {

                        UI.getCurrent().removeWindow(dialog);
                    }
                });


            }
        }

        return deliveryList;
    }



    public void performIt() throws Exception {
        //deliveryPanel.setTheStuff(model.getInitiatedDeliveries());
        infoPanel.setTableContent(model.getTitlesFromFileSystem());
    }
}
