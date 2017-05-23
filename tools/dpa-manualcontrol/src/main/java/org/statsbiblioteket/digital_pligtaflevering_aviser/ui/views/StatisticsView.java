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
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.ConfirmationState;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.NewspaperContextListener;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.NewspaperUI;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.ConfigPanel;

import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.DeliveryOverviewPanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.DeliveryValidationPanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.StatisticsPanels;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.TitleValidationPanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.SearchPanel;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;


/**
 * The full panel for showing deliveries and titles.
 * The panel contains a search panel at the top, a information panel in the middle, and a detail view at the buttom.
 * The information panel can be changed with different panel depending on what is in focus in the view
 */
public class StatisticsView extends VerticalLayout implements View {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private SearchPanel searchPanel = new SearchPanel();
    private DataModel model;
    private Link metadatalink = new Link("Metadatalink", null);
    private Embedded pdfComponent = new Embedded(null, null);
    private StatisticsPanels tabelsLayout;
    private Page currentSelectedPage;
    private Article currentSelectedArticle;

    public StatisticsView(DataModel model, String type) {

        this.model = model;
        MenuBar header = new MenuBar();
        header.setWidth("100%");
        searchPanel.setWidth("100%");
        Layout mainhlayout;
        final VerticalLayout layout = new VerticalLayout();

        pdfComponent.setMimeType("application/pdf");
        pdfComponent.setType(Embedded.TYPE_BROWSER);
        metadatalink.setTargetName("_blank");

        MenuBar.Command configCommand = new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(NewspaperUI.CONFIGPANEL);
            }
        };

        MenuBar.Command otherCommand1 = new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(NewspaperUI.DELIVERYPANEL);
            }
        };

        MenuBar.Command otherCommand2 = new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(NewspaperUI.TITLEVALIDATIONPANEL);
            }
        };

        MenuBar.Command otherCommand3 = new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                getUI().getNavigator().navigateTo(NewspaperUI.OVERVIEW);
            }
        };

        header.addItem("config", configCommand);
        header.addItem("Delivery validation", otherCommand1);
        header.addItem("TitleValidation", otherCommand2);
        header.addItem("Overview", otherCommand3);

        switch (type) {
            case NewspaperUI.DELIVERYPANEL:
                tabelsLayout = new DeliveryValidationPanel(model);
                break;
            case NewspaperUI.TITLEVALIDATIONPANEL:
                tabelsLayout = new TitleValidationPanel(model);
                break;
            case NewspaperUI.OVERVIEW:
                tabelsLayout = new DeliveryOverviewPanel(model);
                break;
            case NewspaperUI.CONFIGPANEL:
                tabelsLayout = new ConfigPanel(model);
                break;
            default:
                tabelsLayout = new DeliveryValidationPanel(model);
        }


        int browserWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
        // The UI is optimized to run on either a small or large screen.
        // A limit of browserscreenwidth 1800 pixels is used.
        // If the browserscreenwidth is large pdfComponent is shown at the right side of the tables, otherwise below.
        // If the browserscreenwidth is large pdfComponent is 900px" X "1200px" otherwise 500px" X "7500px"
        if(browserWidth>1800) {
            mainhlayout = new HorizontalLayout();
            pdfComponent.setWidth("900px");
            pdfComponent.setHeight("1200px");
            tabelsLayout.setHeight("1200px");
        } else {
            mainhlayout = new VerticalLayout();
            pdfComponent.setWidth("500px");
            pdfComponent.setHeight("750px");
        }

        tabelsLayout.setVisible(false);

        final VerticalLayout viewLayout = new VerticalLayout();
        final HorizontalLayout viewControlLayout = new HorizontalLayout();
        mainhlayout.setWidth("100%");
        mainhlayout.setHeight("100%");

        tabelsLayout.setWidth("100%");
        tabelsLayout.setHeight("100%");
        layout.setMargin(true);
        addComponent(layout);

        try {
            searchPanel.setSelectedMonth(model.getSelectedMonth());
        } catch(ParseException e) {
            Notification.show("The application has hit an unexpected incedent, please contact support", Notification.Type.ERROR_MESSAGE);
            log.error("MONTH PARSER ERROR", e);
        }

        searchPanel.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                try {
                    if (SearchPanel.prepareButtonId.equals(event.getButton().getId())) {
                        model.setSelectedMonth(searchPanel.getSelectedDate());
                        model.initiateDeliveries();
                        model.initiateTitleHierachyFromFedora();
                        model.saveCurrentTitleHierachyToFilesystem();
                        panelPrepare(true);
                    } else if (SearchPanel.startButtonId.equals(event.getButton().getId())) {
                        model.setSelectedMonth(searchPanel.getSelectedDate());
                        if(!model.isMonthInitiated()) {
                            Notification.show("This month is not prepared", Notification.Type.ERROR_MESSAGE);
                            tabelsLayout.insertInitialTableValues();
                            panelPrepare(false);
                            return;
                        }
                        model.initiateDeliveries();
                        model.initiateTitleHierachyFromFilesystem();
                        tabelsLayout.insertInitialTableValues();
                        panelPrepare(true);
                    } else if(SearchPanel.linkButtonId.equals(event.getButton().getId())) {

                        URI oldUri = UI.getCurrent().getPage().getLocation();

                        String newQuery = "month=" + model.getSelectedMonthString() + "&" +
                                "del=" + model.getSelectedDelivery() + "&" +
                                "title=" + model.getSelectedTitle();

                        URI newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(), newQuery, oldUri.getFragment());
                        searchPanel.setLabel(newUri.toURL().toString());
                    }

                } catch (Exception e) {
                    Notification.show("The application has hit an unexpected incedent, please contact support", Notification.Type.ERROR_MESSAGE);
                    log.error("Exception has accoured during initialization of datamodel", e);
                }

            }
        });

        tabelsLayout.addFileSelectedListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {


                try {
                    if ("ARTICLE".equals(itemClickEvent.getComponent().getId())) {
                        currentSelectedArticle = (Article) itemClickEvent.getItemId();
                        currentSelectedPage = null;

                        pdfComponent.setVisible(false);

                        Resource resource = new ExternalResource(NewspaperContextListener.fedoraPath + currentSelectedArticle.getId() + "/datastreams/XML/content");
                        metadatalink.setResource(resource);
                        metadatalink.setDescription("Link to Second Page");
                    } else if ("PAGE".equals(itemClickEvent.getComponent().getId())) {
                        currentSelectedPage = (Page) itemClickEvent.getItemId();
                        currentSelectedArticle = null;

                        pdfComponent.setVisible(true);
                        DomsItem domsItem = model.getItemFromUuid(currentSelectedPage.getId()).children().findFirst().get();
                        DomsDatastream pdfStream = domsItem.datastreams().stream().filter(pp -> "CONTENTS".equals(pp.getId())).findFirst().get();
                        URL url = new URL(pdfStream.getUrl());
                        StreamResource streamRecource = createStreamResource(url);
                        pdfComponent.setSource(streamRecource);
                        Resource resource = new ExternalResource(NewspaperContextListener.fedoraPath + currentSelectedPage.getId() + "/datastreams/XML/content");
                        metadatalink.setResource(resource);
                        metadatalink.setDescription("Link to Second Page");
                    }


                } catch (MalformedURLException e) {
                    Notification.show("The application can not create a link to the url, please contact support", Notification.Type.ERROR_MESSAGE);
                    log.error("The link could net parsed into a url", e);
                }
            }
        });


        Button confirmViewButton = new Button("Confirmed");
        confirmViewButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                if(currentSelectedPage!=null) {
                    currentSelectedPage.setCheckedState(ConfirmationState.CHECKED);
                    model.addCheckedPage(currentSelectedPage);
                    tabelsLayout.checkThePage(currentSelectedPage, ConfirmationState.CHECKED);
                }
                if(currentSelectedArticle!=null) {
                    currentSelectedArticle.setCheckedState(ConfirmationState.CHECKED);
                    model.addCheckedArticle(currentSelectedArticle);
                    tabelsLayout.checkTheArticle(currentSelectedArticle, ConfirmationState.CHECKED);
                }
            }
        });

        Button rejectViewButton = new Button("Reject");
        rejectViewButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                if(currentSelectedPage!=null) {
                    currentSelectedPage.setCheckedState(ConfirmationState.REJECTED);
                    model.addCheckedPage(currentSelectedPage);
                    tabelsLayout.checkThePage(currentSelectedPage, ConfirmationState.REJECTED);
                }
                if(currentSelectedArticle!=null) {
                    currentSelectedArticle.setCheckedState(ConfirmationState.REJECTED);
                    model.addCheckedArticle(currentSelectedArticle);
                    tabelsLayout.checkTheArticle(currentSelectedArticle, ConfirmationState.REJECTED);
                }
            }
        });

        layout.addComponent(header);
        layout.addComponent(searchPanel);

        mainhlayout.addComponent(tabelsLayout);

        viewControlLayout.addComponent(confirmViewButton);
        viewControlLayout.addComponent(rejectViewButton);
        viewControlLayout.addComponent(metadatalink);

        viewLayout.addComponent(viewControlLayout);
        viewLayout.addComponent(pdfComponent);

        mainhlayout.addComponent(viewLayout);
        layout.addComponent(mainhlayout);
        panelPrepare(false);
    }

    /**
     * Set panes to being prepared for viewing details
     * @param prepare
     */
    private void panelPrepare(boolean prepare) {
        pdfComponent.setVisible(prepare);
        metadatalink.setVisible(prepare);
        tabelsLayout.setVisible(prepare);
    }

    /**
     * Convert the URI to the pdfComponent-file into a StreamResource for viewing in UI
     * @param url
     * @return
     * @throws Exception
     */
    private synchronized StreamResource createStreamResource(final java.net.URL url) {

        final StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                try {
                    InputStream inps = url.openStream();
                    return inps;
                } catch (IOException e) {
                    Notification.show("The application can not read the pdf-file", Notification.Type.WARNING_MESSAGE);
                    log.error("The stream could not get opened", e);
                    return null;
                }
            }
        }, "pages.pdf"  );// Short pagename is needed
        resource.setMIMEType("application/pdf");
        resource.setCacheTime(1000);
        return resource;
    }

    /**
     * Show a welcome message when entering the page
     * If the model has been initiated with search-selections these wil be shown
     * @param event
     */
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        try {
            searchPanel.setSelectedMonth(model.getSelectedMonth());
            if(model.getSelectedDelivery()!=null && model.getSelectedTitle()!=null) {
                model.initiateDeliveries();
                model.initiateTitleHierachyFromFilesystem();
                tabelsLayout.insertInitialTableValues();
                panelPrepare(true);
            }

        } catch (Exception e) {
            Notification.show("The application has hit an unexpected incedent, please contact support", Notification.Type.ERROR_MESSAGE);
            log.error("Initialization of model during entering StatisticsView has failed", e);
        }
        tabelsLayout.viewIsEntered();
        Notification.show("DPA Delivery validation");
    }

}