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


    private SingleStringListPanel infoPanel = new SingleStringListPanel();
    private TitleListPanel titPanel = new TitleListPanel();
    private DataModel model;
    //private FileListTable table1 = new FileListTable(Article.class);
    private FileListTable fileSelectionPanel = new FileListTable(Page.class);

    public DeliveryInformationPanel(DataModel model) {
        this.setWidth("100%");
        this.setHeight("100%");

        this.model = model;
        //this.parser = parser;
        //table1.setEnabled(false);
        fileSelectionPanel.setEnabled(false);

        infoPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {


                    com.vaadin.data.Item selectedItem = itemClickEvent.getItem();
                    DomsItem dItem = infoPanel.getDomsItem(selectedItem);

                    //DeliveryStatistics delStat =parser.processDomsIdToStream().apply(dItem);



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

                            //File tempFile = new File("/tmp/pathstream");
                            JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryStatistics.class);
                            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                            DeliveryStatistics deserializedObject = (DeliveryStatistics)jaxbUnmarshaller.unmarshal(inps);

                            titPanel.setInfo(deserializedObject);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    //table1.setEnabled(false);
                    fileSelectionPanel.setEnabled(false);



            }
        });



        titPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
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

        this.addComponent(infoPanel);
        this.addComponent(titPanel);
        //this.addComponent(table1);
        this.addComponent(fileSelectionPanel);
    }

    public void getTitles() {

        //titPanel.getTitles();

    }


    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {
        fileSelectionPanel.addItemClickListener(listener);
    }


    public void performInitialSearch(FetchEventStructure eventStructureCommunication, String info) {
        infoPanel.setInfo(eventStructureCommunication, "Data_Archived");

        /*ArrayList<String> list = model.getDeliveries("Data_Archived");
        infoPanel.setTheStuff(list);*/
    }
}
