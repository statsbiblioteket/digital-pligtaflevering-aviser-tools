package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.views;

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
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.DeliveryMainPanel;

import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.DeliveryInformationPanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.DeliveryInformationPanel2;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.SearchPanel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;


/**
 * Created by mmj on 3/8/17.
 */
public class StatisticsView extends VerticalLayout implements View {

    private DataModel model = new DataModel();
    private Link link = new Link("Metadatlink", null);
    private Embedded pdf = new Embedded(null, null);
    private String bitRepoPath = "http://172.18.100.153:58709/var/file1pillar/files/dpaviser/folderDir/";
    private String fedoraPath = "http://localhost:7880/fedora/objects/";
    private Page currentSelectedPage;

    public StatisticsView(String type) {

        Layout mainhlayout;
        final VerticalLayout layout = new VerticalLayout();
        DeliveryMainPanel tabelsLayout;

        pdf.setMimeType("application/pdf");
        pdf.setType(Embedded.TYPE_BROWSER);
        link.setTargetName("_blank");


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
                pdf.setWidth("900px");
                pdf.setHeight("1300px");
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
                pdf.setWidth("900px");
                pdf.setHeight("1300px");
                tabelsLayout.setHeight("1500px");
                break;
            default:
                tabelsLayout = new DeliveryInformationPanel(model);
                mainhlayout = new HorizontalLayout();
        }

        final VerticalLayout viewLayout = new VerticalLayout();
        final HorizontalLayout viewControlLayout = new HorizontalLayout();
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
                    model.initiateTitleHierachyFromFedora();
                    model.saveCurrentTitleHierachyToFilesystem(searchPanel.getSelectedDate());
                } else if("SAVECHECK".equals(event.getButton().getId())) {






                    tabelsLayout.runThrough();









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

                    currentSelectedPage = (Page)itemClickEvent.getItemId();

                    StreamResource streamRecource= createStreamResource(currentSelectedPage.getPageName());
                    pdf.setSource(streamRecource);

                    Resource resource = new ExternalResource(fedoraPath + currentSelectedPage.getId() + "/datastreams/XML/content");
                    link.setResource(resource);
                    link.setDescription("Link to Second Page");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        Button confirmViewButton = new Button("Confirmed");
        confirmViewButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                model.addTextToCurrentItem(currentSelectedPage);

            }});


        layout.addComponent(searchPanel);

        mainhlayout.addComponent(tabelsLayout);

        viewControlLayout.addComponent(confirmViewButton);
        viewControlLayout.addComponent(link);

        viewLayout.addComponent(viewControlLayout);
        viewLayout.addComponent(pdf);

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

                    String pathString = bitRepoPath +fileUrl.replaceAll("#", "%23")+".pdf";
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