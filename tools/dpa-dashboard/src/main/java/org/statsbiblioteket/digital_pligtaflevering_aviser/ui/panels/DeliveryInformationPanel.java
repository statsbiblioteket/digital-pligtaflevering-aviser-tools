package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.Wrapper;
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
                model.setSelectedDelivery(selectedDelivery);

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

                List<Page> pages = ((Title)itemClickEvent.getItemId()).getPage();

                sectionSectionTable.setInfo(UiDataConverter.sectionConverter(pages.iterator()).values());

                fileSelectionPanel.setEnabled(true);
                fileSelectionPanel.setInfo(pages);
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

    public Wrapper getTitles() {

        return titleListPanel.getTitles();
    }


    public void performIt() {
        deliveryListPanel.setTheStuff(model.getInitiatedDeliveries());
    }
}
