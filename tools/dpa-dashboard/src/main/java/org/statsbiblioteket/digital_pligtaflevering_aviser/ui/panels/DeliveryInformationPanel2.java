package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryIdentifier;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.ResultStorePanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows.StoreResultWindow;

import java.util.ArrayList;
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
                selectedSection = null;
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
                selectedSection = null;
                showTheSelectedTitle();

            }
        });

        sectionSectionTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object selection = itemClickEvent.getItem().getItemProperty("sectionNumber").getValue();
                selectedSection = selection.toString();
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


        fileSelectionPanel.setEnabled(false);

        String selectedDelivery = model.getSelectedDelivery();
        String selectedTitle = model.getSelectedTitle();
        if(selectedDelivery==null || selectedTitle==null) {
            return;
        }

        Title title = model.getTitleObj(selectedDelivery, selectedTitle);
        if(title==null) {
            return;
        }

        fileSelectionPanel.setEnabled(true);

        List<Page> pages = title.getPage();

        if(selectedSection != null) {
            List<Page> filteredPages = new ArrayList<Page>();
            for(Page page : pages) {
                if(selectedSection.equals(page.getSectionNumber())) {
                    filteredPages.add(page);
                }
            }
            fileSelectionPanel.setInfo(filteredPages);
        } else {
            sectionSectionTable.cleanTable();
            sectionSectionTable.setInfo(UiDataConverter.sectionConverter(title.getPage().iterator(), null).values());
            fileSelectionPanel.setInfo(pages);
        }
    }

    public List runThrough() {

        DeliveryIdentifier deliveryList = (DeliveryIdentifier)deliveryPanel.getSelection();

        /*for(DeliveryIdentifier deliveryId : deliveryList) {
            if(deliveryId.isChecked()) {*/

                String selectedDelivery = model.getSelectedDelivery();
                String selectedTitle = model.getSelectedTitle();

                DeliveryIdentifier item = model.getCurrentDelItem();


                final StoreResultWindow dialog = new StoreResultWindow(selectedTitle + " - " + selectedDelivery);
                ResultStorePanel storePanel = new ResultStorePanel();

                dialog.setDialogContent(storePanel);
                dialog.setValues(item);
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
            /*}
        }*/
        return null;
    }



    public void performIt() throws Exception {
        //deliveryPanel.setTheStuff(model.getInitiatedDeliveries());
        infoPanel.setTableContent(model.getTitlesFromFileSystem());
    }
}