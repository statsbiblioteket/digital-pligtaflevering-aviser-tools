package org.kb.ui.views;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.DownloadStream;
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
import org.kb.ui.datamodel.DataModel;
import org.kb.ui.FetchEventStructure;
import org.kb.ui.panels.DeliveryInformationPanel;
import org.kb.ui.panels.DeliveryInformationPanel2;
import org.kb.ui.panels.DeliveryMainPanel;
import org.kb.ui.panels.SearchPanel;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Date;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.name;

/**
 * Created by mmj on 3/8/17.
 */
public class StatisticsView extends VerticalLayout implements View {

    private FetchEventStructure eventStructureCommunication = new FetchEventStructure();
    private DataModel model = new DataModel();



    public StatisticsView(String type) {

        Layout mainhlayout;

        final VerticalLayout layout = new VerticalLayout();

        DeliveryMainPanel tabelsLayout;
        Link link = new Link("Metadatlink", null);
        Embedded pdf = new Embedded(null, null);
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

        SearchPanel button = new SearchPanel();
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                if("SEARCHBUTTON".equals(event.getButton().getId())) {

                    tabelsLayout.performInitialSearch(eventStructureCommunication, "Data_Archived");

                } else if("STOREBUTTON".equals(event.getButton().getId())) {

                    tabelsLayout.getTitles();
                }

            }
        });

        tabelsLayout.addFileSelectedListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {

                try {
                    Object page = itemClickEvent.getItem().getItemProperty("pageName");
                    String path = "http://172.18.100.153:58709/var/file1pillar/files/dpaviser/folderDir/";
                    StreamResource recou= createStreamResource(path+URLEncoder.encode(page.toString(), "UTF-8")+".pdf");
                    pdf.setSource(recou);

                    Resource resource = new ExternalResource(path+URLEncoder.encode(page.toString(), "UTF-8")+".xml");
                    link.setResource(resource);
                    link.setDescription("Link to Second Page");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        layout.addComponent(button);

        mainhlayout.addComponent(tabelsLayout);
        viewLayout.addComponent(pdf);
        viewLayout.addComponent(link);
        mainhlayout.addComponent(viewLayout);
        layout.addComponent(mainhlayout);
    }



    private StreamResource createStreamResource(final String fileUrl) {
        StreamResource recource = new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                try {
                    URL uu = new URL(fileUrl);
                    InputStream inps = uu.openStream();
                    return inps;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }, "fileUrl.pdf");
        recource.setMIMEType("application/pdf");
        return recource;
    }


    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Notification.show("Welcome to Status");
    }

}