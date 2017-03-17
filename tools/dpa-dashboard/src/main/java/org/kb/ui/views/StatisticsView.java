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
import org.kb.ui.datamodel.DataModel;
import org.kb.ui.FetchEventStructure;
import org.kb.ui.panels.DeliveryInformationPanel;
import org.kb.ui.panels.DeliveryInformationPanel2;
import org.kb.ui.panels.DeliveryMainPanel;
import org.kb.ui.panels.SearchPanel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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

                    //System.out.println(pp);
                }

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


        layout.addComponent(button);

        mainhlayout.addComponent(tabelsLayout);
        viewLayout.addComponent(pdf);
        mainhlayout.addComponent(viewLayout);
        layout.addComponent(mainhlayout);
    }


    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Notification.show("Welcome to Status");
    }

}