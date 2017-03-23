package org.kb.ui.panels;

import com.vaadin.event.ItemClickEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.kb.ui.FetchEventStructure;
import org.kb.ui.datamodel.DataModel;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;


/**
 * Created by mmj on 3/10/17.
 */
public class DeliveryInformationPanel extends DeliveryMainPanel {


    private DeliveryListPanel deliveryListPanel = new DeliveryListPanel();
    private TitleListPanel titleListPanel = new TitleListPanel();

    public DeliveryInformationPanel(DataModel model) {
        super(model);
        //table1.setEnabled(false);
        fileSelectionPanel.setEnabled(false);

        deliveryListPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {

                com.vaadin.data.Item selectedItem = itemClickEvent.getItem();
                String selectedDelivery = selectedItem.getItemProperty("Name").getValue().toString();
                DomsItem dItem = model.getDeliveryFromName(selectedDelivery);

                    //DomsItem dItem = deliveryListPanel.getDomsItem(selectedItem);

                    final List<DomsDatastream> datastreams = dItem.datastreams();
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

                            titleListPanel.setInfo(deserializedObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    //table1.setEnabled(false);
                    fileSelectionPanel.setEnabled(false);



            }
        });



        titleListPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                /*table1.setCaption(((Title)itemClickEvent.getItemId()).getTitle());
                table1.setEnabled(true);
                table1.setInfo(((Title)itemClickEvent.getItemId()).getArticle());
                table1.setCaption(((Title)itemClickEvent.getItemId()).getTitle());*/
                fileSelectionPanel.setEnabled(true);
                fileSelectionPanel.setInfo(((Title)itemClickEvent.getItemId()).getPage());
            }
        });


        this.addComponent(deliveryListPanel);
        this.addComponent(titleListPanel);
        this.addComponent(dummySectionTable);
        this.addComponent(fileSelectionPanel);


        this.setExpandRatio(deliveryListPanel, 0.2f);
        this.setExpandRatio(titleListPanel, 0.2f);
        this.setExpandRatio(dummySectionTable, 0.2f);
        this.setExpandRatio(fileSelectionPanel, 0.4f);

    }

    public void getTitles() {

        //titleListPanel.getTitles();

    }





    public void performInitialSearch(FetchEventStructure eventStructureCommunication, String info) {

        model.initiateDeliveries("Data_Archived");
        deliveryListPanel.setTheStuff(model.getInitiatedDeliveries());

        //deliveryListPanel.setInfo(eventStructureCommunication, "Data_Archived");

        /*ArrayList<String> list = model.getDeliveries("Data_Archived");
        deliveryListPanel.setTheStuff(list);*/
    }
}
