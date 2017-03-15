package org.kb.ui.views;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsParser;
import org.kb.ui.DataModel;
import org.kb.ui.FetchEventStructure;
import org.kb.ui.NewspaperUI;
import org.kb.ui.panels.DeliveryInformationPanel;
import org.kb.ui.panels.DeliveryInformationPanel2;
import org.kb.ui.panels.DeliveryMainPanel;
import org.kb.ui.panels.SearchPanel;
import org.kb.ui.panels.XmlView;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.URLEncoder;

/**
 * Created by mmj on 3/8/17.
 */
public class StatisticsView extends VerticalLayout implements View {

    private FetchEventStructure eventStructureCommunication = new FetchEventStructure();
    private DataModel model = new DataModel();
    private DomsParser parser = new DomsParser();


    public StatisticsView(String type) {

        final VerticalLayout mainhlayout = new VerticalLayout();
        final VerticalLayout layout = new VerticalLayout();

        DeliveryMainPanel tabelsLayout;

        if("Std".equals(type)) {
            tabelsLayout = new DeliveryInformationPanel(parser);
        } else {
            tabelsLayout = new DeliveryInformationPanel2(parser);
        }




        final HorizontalLayout viewLayout = new HorizontalLayout();
        mainhlayout.setWidth("100%");
        mainhlayout.setHeight("100%");

        tabelsLayout.setWidth("100%");
        layout.setMargin(true);
        addComponent(layout);

        Embedded pdf = new Embedded(null, null);

        XmlView treeView = new XmlView();
        treeView.setWidth("500px");



        SearchPanel button = new SearchPanel();
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                tabelsLayout.setBatch(eventStructureCommunication, "Data_Archived");
            }
        });

        tabelsLayout.addFileSelectedListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {

                try {

                    Object page = itemClickEvent.getItem().getItemProperty("pageName");


                    String fileUrl = "http://172.18.100.153:58709/var/reference1pillar/dpaviser/folderDir/"+ URLEncoder.encode(page+"", "UTF-8");
                    pdf.setSource(new ExternalResource(fileUrl+".pdf"));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        pdf.setMimeType("application/pdf");
        pdf.setType(Embedded.TYPE_BROWSER);
        pdf.setHeight("500px");

        layout.addComponent(button);

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