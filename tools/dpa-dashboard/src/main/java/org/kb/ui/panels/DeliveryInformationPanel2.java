package org.kb.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.kb.ui.datamodel.DataModel;
import org.kb.ui.datamodel.DeliveryIdentifier;
import org.kb.ui.datamodel.UiDataConverter;
import org.kb.ui.windows.ResultStorePanel;
import org.kb.ui.windows.StoreResultWindow;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


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
                showTheSelectedTitle();
                List<DeliveryIdentifier> list = model.getDeliverysFromTitle(selectedTitle);

                deliveryPanel.setInfo(list);


            }
        });


        deliveryPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("title").getValue();
                model.setSelectedDelivery(page.toString());
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


        final List<DomsDatastream> datastreams = model.getDeliveryFromName(selectedDelivery).datastreams();
        Optional<DomsDatastream> profileOptional = datastreams.stream()
                .filter(ds -> ds.getId().equals("DELIVERYSTATISTICS"))
                .findAny();


        if (profileOptional.isPresent()) {
            try {
                DomsDatastream ds = profileOptional.get();
                //We are reading this textstring as a String and are aware that thish might leed to encoding problems
                StringReader reader = new StringReader(ds.getDatastreamAsString());
                InputSource inps = new InputSource(reader);

                JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryStatistics.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                DeliveryStatistics deserializedObject = (DeliveryStatistics)jaxbUnmarshaller.unmarshal(inps);


                Title selectedTitleObj = null;
                List<Title> titleList = deserializedObject.getTitles().getTitles();
                for(Title title : titleList) {
                    if(selectedTitle.equals(title.getTitle())) {
                        selectedTitleObj = title;
                    }
                }
                if(selectedTitleObj==null) {
                    return;
                }

                Iterator<Page> it = selectedTitleObj.getPage().iterator();
                sectionSectionTable.setInfo(UiDataConverter.sectionConverter(it).values());

                fileSelectionPanel.setEnabled(true);
                fileSelectionPanel.setInfo(selectedTitleObj.getPage());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List runThrough() {

        List<DeliveryIdentifier> deliveryList = deliveryPanel.getSelections();

        for(DeliveryIdentifier deliveryId : deliveryList) {
            if(deliveryId.isChecked()) {

                String selectedDelivery = model.getSelectedDelivery();
                String selectedTitle = model.getSelectedTitle();

                final StoreResultWindow dialog = new StoreResultWindow(selectedTitle + " - " + selectedDelivery);
                dialog.setDialogContent(new ResultStorePanel());
                dialog.setModal(true);


                UI.getCurrent().addWindow(dialog);
                dialog.setListener(new Button.ClickListener() {
                    public void buttonClick(Button.ClickEvent event) {

                        UI.getCurrent().removeWindow(dialog);
                        model.writeToCurrentItemCashed(selectedDelivery, selectedTitle, true, "TESTING");

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
        infoPanel.setTableContent(model.getTitles());
    }
}
