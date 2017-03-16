package org.kb.ui.panels;

import com.vaadin.event.ItemClickEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.kb.ui.FetchEventStructure;
import org.kb.ui.datamodel.DataModel;
import org.kb.ui.datamodel.DataformatConverter;
import org.kb.ui.datamodel.FileComponent;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * Created by mmj on 3/10/17.
 */
public class DeliveryInformationPanel2 extends DeliveryMainPanel {


    private DataModel model;
    private DeliveryListPanel infoPanel = new DeliveryListPanel();
    private FileListTable table = new FileListTable(Page.class);
    private ArrayList<FileComponent> alr = new ArrayList<FileComponent>();


    public DeliveryInformationPanel2(DataModel model) {
        this.setWidth("100%");


        infoPanel.setTheStuff(model.getTitles());


        this.model = model;
        this.addComponent(infoPanel);
        //this.addComponent(titPanel);
        //this.addComponent(table1);
        this.addComponent(table);
    }

    public void addFileSelectedListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }


    public void setBatch(FetchEventStructure eventStructureCommunication, String info) {

        table.setEnabled(true);
        DataformatConverter yy = new DataformatConverter();

        Stream<DomsItem> items = eventStructureCommunication.getState(info);

        items.forEach(new Consumer<DomsItem>() {
            @Override
            public void accept(final DomsItem o) {
                /*Object newItemId = table.addItem();
                com.vaadin.data.Item row1 = table.getItem(newItemId);
                row1.getItemProperty("Batch").setValue(o.getPath());
                itemList.put(row1, o);*/




                final List<DomsDatastream> datastreams = o.datastreams();
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
                        yy.setIt(deserializedObject);



                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }







                //delStat.






                /*for(Title title : delStat.getTitles().getTitles()) {

                    List<Article> articles = title.getArticle();
                    for(Article art : articles) {
                        alr.add(new FileComponent(o.getPath(),title.getTitle(),art.getArticleName()));
                    }

                    List<Page> pages = title.getPage();
                    for(Page art : pages) {
                        alr.add(new FileComponent(o.getPath(),title.getTitle(),art.getPageName()));
                    }

                }*/







            }

        });

        ArrayList<Title> titleList = yy.getIt("bt");


        table.setCaption("bt");
        table.setInfo(titleList.get(0).getPage());


        //table.setInfo(alr);




        /*for(Object o : delStat) {
            beans.addBean(o);
        }*/

    }
}
