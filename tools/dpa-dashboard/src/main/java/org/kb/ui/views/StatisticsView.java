package org.kb.ui.views;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import org.kb.ui.datamodel.DataModel;

import org.kb.ui.datamodel.TitleDeliveryHierachy;
import org.kb.ui.panels.DeliveryInformationPanel;
import org.kb.ui.panels.DeliveryInformationPanel2;
import org.kb.ui.panels.DeliveryMainPanel;
import org.kb.ui.panels.SearchPanel;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


/**
 * Created by mmj on 3/8/17.
 */
public class StatisticsView extends VerticalLayout implements View {

    private DataModel model = new DataModel();
    Link link = new Link("Metadatlink", null);
    Embedded pdf = new Embedded(null, null);
    String path = "http://172.18.100.153:58709/var/file1pillar/files/dpaviser/folderDir/";

    public StatisticsView(String type) {

        Layout mainhlayout;

        final VerticalLayout layout = new VerticalLayout();

        DeliveryMainPanel tabelsLayout;


        pdf.setMimeType("application/pdf");
        pdf.setType(Embedded.TYPE_BROWSER);


        switch(type) {
            case "1v1":
                tabelsLayout = new DeliveryInformationPanel(model);
                mainhlayout = new VerticalLayout();
                pdf.setWidth("500px");
                pdf.setHeight("750px");
                break;
            case "1v2":
                tabelsLayout = new DeliveryInformationPanel(model);
                mainhlayout = new HorizontalLayout();
                pdf.setWidth("1000px");
                pdf.setHeight("1500px");
                tabelsLayout.setHeight("1500px");
                break;
            case "2v1":
                tabelsLayout = new DeliveryInformationPanel2(model);
                mainhlayout = new VerticalLayout();
                pdf.setWidth("500px");
                pdf.setHeight("750px");
                break;
            case "2v2":
                tabelsLayout = new DeliveryInformationPanel2(model);
                mainhlayout = new HorizontalLayout();
                pdf.setWidth("1000px");
                pdf.setHeight("1500px");
                tabelsLayout.setHeight("1500px");
                break;
            default:
                tabelsLayout = new DeliveryInformationPanel(model);
                mainhlayout = new HorizontalLayout();
        }

        final HorizontalLayout viewLayout = new HorizontalLayout();
        mainhlayout.setWidth("100%");
        mainhlayout.setHeight("100%");

        tabelsLayout.setWidth("100%");
        tabelsLayout.setHeight("100%");
        layout.setMargin(true);
        addComponent(layout);

        SearchPanel searchPanel = new SearchPanel();
        searchPanel.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                try {

                if("SEARCHBUTTON".equals(event.getButton().getId())) {
                    model.setSelectedMonth(searchPanel.getSelectedDate());
                    model.initiateDeliveries("Data_Archived");
                    tabelsLayout.performIt();

                } else if("STOREBUTTON".equals(event.getButton().getId())) {

                    model.initiateTitleHierachy();
                    model.saveCurrentTitleHierachy(searchPanel.getSelectedDate());
                }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        tabelsLayout.addFileSelectedListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {

                try {
                    Object page = itemClickEvent.getItem().getItemProperty("pageName");


                    StreamResource recou= createStreamResource(page.toString());
                    pdf.setSource(recou);

                    Resource resource = new ExternalResource(path+URLEncoder.encode(page.toString(), "UTF-8")+".xml");
                    link.setResource(resource);
                    link.setDescription("Link to Second Page");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        layout.addComponent(searchPanel);

        mainhlayout.addComponent(tabelsLayout);
        viewLayout.addComponent(pdf);
        viewLayout.addComponent(link);
        mainhlayout.addComponent(viewLayout);
        layout.addComponent(mainhlayout);
    }

    int count = 0;//TODO: MAKE THAT BETTER


    private StreamResource createStreamResource(final String fileUrl) {
        count++;
        StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                try {

                    String pathString = path+fileUrl.replaceAll("#", "%23")+".pdf";
                    System.out.println(pathString);

                    URL uu = new URL(pathString);
                    InputStream inps = uu.openStream();
                    return inps;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }, "tst" + count + ".pdf");
        resource.setMIMEType("application/pdf");
        resource.setCacheTime(1000);
        return resource;
    }


    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Notification.show("DPA Delivery validation");
    }

}