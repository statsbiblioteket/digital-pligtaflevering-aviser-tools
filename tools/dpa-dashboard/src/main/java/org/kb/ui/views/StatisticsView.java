package org.kb.ui.views;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsParser;
import org.kb.ui.DataModel;
import org.kb.ui.panels.FileListTable;
import org.kb.ui.panels.DeliveryListPanel;
import org.kb.ui.FetchEventStructure;
import org.kb.ui.panels.SearchPanel;
import org.kb.ui.panels.TitleListPanel;
import org.kb.ui.panels.XmlView;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
        final HorizontalLayout tabelsLayout = new HorizontalLayout();
        final HorizontalLayout viewLayout = new HorizontalLayout();
        mainhlayout.setWidth("100%");
        mainhlayout.setHeight("100%");

        tabelsLayout.setWidth("100%");
        layout.setMargin(true);
        addComponent(layout);

        DeliveryListPanel infoPanel = new DeliveryListPanel();

        TitleListPanel titPanel = new TitleListPanel();

        FileListTable table1 = new FileListTable(Article.class);
        table1.setEnabled(false);

        FileListTable table2 = new FileListTable(Page.class);
        table2.setEnabled(false);


        Embedded pdf = new Embedded(null, null);

        XmlView treeView = new XmlView();
        treeView.setWidth("500px");



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
                table1.setCaption(((Title)itemClickEvent.getItemId()).getTitle());
                table1.setEnabled(true);
                table1.setInfo(((Title)itemClickEvent.getItemId()).getArticle());
                table1.setCaption(((Title)itemClickEvent.getItemId()).getTitle());
                table2.setEnabled(true);
                table2.setInfo(((Title)itemClickEvent.getItemId()).getPage());
            }
        });


        table2.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {

                try {
                    String fileUrl = "http://localhost:58709/var/reference1pillar/dpaviser/folderDir/"+ URLEncoder.encode(itemClickEvent.getItem().toString()+"", "UTF-8");
                    pdf.setSource(new ExternalResource(fileUrl+".pdf"));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



        File pdfFile = new File("");
        //Embedded pdf = new Embedded(null, new FileResource(pdfFile));

        pdf.setMimeType("application/pdf");
        pdf.setType(Embedded.TYPE_BROWSER);
        pdf.setHeight("500px");
        addComponent(pdf);

        layout.addComponent(button);
        tabelsLayout.addComponent(infoPanel);
        tabelsLayout.addComponent(titPanel);
        tabelsLayout.addComponent(table1);
        tabelsLayout.addComponent(table2);

        mainhlayout.addComponent(tabelsLayout);
        viewLayout.addComponent(pdf);
        viewLayout.addComponent(treeView);
        mainhlayout.addComponent(viewLayout);
        layout.addComponent(mainhlayout);
    }


    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Notification.show("Welcome to Status");
    }

}