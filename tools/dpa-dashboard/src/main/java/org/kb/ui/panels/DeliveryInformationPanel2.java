package org.kb.ui.panels;

import com.vaadin.event.ItemClickEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.kb.ui.FetchEventStructure;
import org.kb.ui.datamodel.DataModel;
import org.kb.ui.datamodel.FileComponent;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Created by mmj on 3/10/17.
 */
public class DeliveryInformationPanel2 extends DeliveryMainPanel {


    private DataModel model;
    private SingleStringListPanel infoPanel = new SingleStringListPanel();
    private SingleStringListPanel deliveryPanel = new SingleStringListPanel();
    private SingleStringListPanel dummySectionTable = new SingleStringListPanel();
    private FileListTable fileSelectionPanel = new FileListTable(Page.class);
    private ArrayList<FileComponent> alr = new ArrayList<FileComponent>();


    public DeliveryInformationPanel2(DataModel model) {
        this.setWidth("100%");


        infoPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("Item").getValue();
                model.setSelectedTitle(page.toString());
                showTheSelectedPage();
            }
        });


        deliveryPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                Object page = itemClickEvent.getItem().getItemProperty("Item").getValue();
                model.setSelectedDelivery(page.toString());
                showTheSelectedPage();
            }
        });




        this.model = model;
        this.addComponent(infoPanel);
        this.addComponent(deliveryPanel);
        this.addComponent(dummySectionTable);
        this.addComponent(fileSelectionPanel);
    }

    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {
        fileSelectionPanel.addItemClickListener(listener);
    }

    private void showTheSelectedPage() {

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

                //File tempFile = new File("/tmp/pathstream");
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


                fileSelectionPanel.setEnabled(true);
                fileSelectionPanel.setInfo(selectedTitleObj.getPage());

                System.out.println(deserializedObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public void performInitialSearch(FetchEventStructure eventStructureCommunication, String info) {

        model.initiateDeliveries("Data_Archived");
        deliveryPanel.setTheStuff(model.getInitiatedDeliveries());
        infoPanel.setTheStuff(model.getTitles());
    }
}
