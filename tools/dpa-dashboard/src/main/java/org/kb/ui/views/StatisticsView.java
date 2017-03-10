package org.kb.ui.views;

import com.sun.jersey.api.client.WebResource;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsParser;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import org.kb.ui.DataModel;
import org.kb.ui.panels.FileListTable;
import org.kb.ui.panels.DeliveryListPanel;
import org.kb.ui.FetchEventStructure;

import org.kb.ui.panels.SearchPanel;
import org.kb.ui.panels.TitleListPanel;

import java.io.File;
import java.util.stream.Stream;

/**
 * Created by mmj on 3/8/17.
 */
public class StatisticsView extends VerticalLayout implements View {

    private FetchEventStructure eventStructureCommunication = new FetchEventStructure();
    private DataModel model = new DataModel();
    private DomsParser parser = new DomsParser();


    public StatisticsView() {

        final VerticalLayout mainhlayout = new VerticalLayout();
        final VerticalLayout layout = new VerticalLayout();
        final HorizontalLayout hlayout = new HorizontalLayout();
        mainhlayout.setWidth("100%");
        mainhlayout.setHeight("100%");

        hlayout.setWidth("100%");
        layout.setMargin(true);
        addComponent(layout);

        DeliveryListPanel infoPanel = new DeliveryListPanel();

        TitleListPanel titPanel = new TitleListPanel();

        FileListTable table1 = new FileListTable(Article.class);
        table1.setEnabled(false);

        FileListTable table2 = new FileListTable(Page.class);
        table2.setEnabled(false);


        Embedded pdf = new Embedded(null, null);



        SearchPanel button = new SearchPanel();
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                infoPanel.setInfo(eventStructureCommunication, "Data_Archived");
            }
        });


        infoPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {

                com.vaadin.data.Item selectedItem = itemClickEvent.getItem();


                String heading = (String)(selectedItem.getItemProperty("Batch").getValue());

                DomsItem dItem = infoPanel.getDomsItem(selectedItem);

                DeliveryStatistics delStat =parser.processDomsIdToStream().apply(dItem);

                titPanel.setInfo(delStat);
                table1.setEnabled(false);
                table2.setEnabled(false);
            }
        });


        titPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                table1.setEnabled(true);
                table1.setInfo(((Title)itemClickEvent.getItemId()).getArticle());
                table2.setEnabled(true);
                table2.setInfo(((Title)itemClickEvent.getItemId()).getPage());
            }
        });


        table2.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                pdf.setSource(new ExternalResource("http://localhost:58709/var/reference1pillar/dpaviser/folderDir/dl_20160913_rt1%2Ftestavis%2Fpages%2F20160913_testavis_page002_t20160811x1%230002.pdf"));
            }
        });



        File pdfFile = new File("");
        //Embedded pdf = new Embedded(null, new FileResource(pdfFile));

        pdf.setMimeType("application/pdf");
        pdf.setType(Embedded.TYPE_BROWSER);
        pdf.setHeight("500px");
        addComponent(pdf);

        layout.addComponent(button);
        hlayout.addComponent(infoPanel);
        hlayout.addComponent(titPanel);
        hlayout.addComponent(table1);
        hlayout.addComponent(table2);

        mainhlayout.addComponent(hlayout);
        mainhlayout.addComponent(pdf);
        layout.addComponent(mainhlayout);
    }


    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Notification.show("Welcome to Status");
    }

}