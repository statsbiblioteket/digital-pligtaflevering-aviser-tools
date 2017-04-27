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

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.ConfirmationState;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.NewspaperUI;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.ConfigPanel;

import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.DeliveryOverviewPanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.DeliveryValidationPanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.StatisticsPanels;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.TitleValidationPanel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.SearchPanel;

import java.io.InputStream;
import java.net.URI;


/**
 * The full panel for showing deliveries and titles
 */
public class StatisticsView extends VerticalLayout implements View {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private DataModel model = new DataModel();
    private Link link = new Link("Metadatlink", null);
    private Embedded pdf = new Embedded(null, null);
    private StatisticsPanels tabelsLayout;
    private String bitRepoPath = "http://172.18.100.153:58709/var/file1pillar/files/dpaviser/folderDir/";
    private String fedoraPath = "http://172.18.100.153:7880/fedora/objects/";
    private Page currentSelectedPage;
    private Article currentSelectedArticle;

    public StatisticsView(String type) {

        MenuBar header = new MenuBar();
        header.setWidth("100%");
        Layout mainhlayout;
        final VerticalLayout layout = new VerticalLayout();

        pdf.setMimeType("application/pdf");
        pdf.setType(Embedded.TYPE_BROWSER);
        link.setTargetName("_blank");

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
        if(browserWidth>1800) {
            mainhlayout = new HorizontalLayout();
            pdf.setWidth("900px");
            pdf.setHeight("1200px");
            tabelsLayout.setHeight("1200px");
        } else {
            mainhlayout = new VerticalLayout();
            pdf.setWidth("500px");
            pdf.setHeight("750px");
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

        SearchPanel searchPanel = new SearchPanel();
        searchPanel.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                try {

                    if ("PREPAREBUTTON".equals(event.getButton().getId())) {
                        model.setSelectedMonth(searchPanel.getSelectedDate());
                        model.initiateDeliveries(searchPanel.useAllreadyValidated());
                        model.initiateTitleHierachyFromFedora();
                        model.saveCurrentTitleHierachyToFilesystem();
                        panelPrepare(true);
                    } else if ("START".equals(event.getButton().getId())) {
                        model.setSelectedMonth(searchPanel.getSelectedDate());
                        if(!model.isMonthInitiated()) {
                            Notification.show("This month is not prepared", Notification.Type.ERROR_MESSAGE);
                            tabelsLayout.insertInitialTableValues();
                            panelPrepare(false);
                            return;
                        }
                        model.initiateDeliveries(searchPanel.useAllreadyValidated());
                        model.initiateTitleHierachyFromFilesystem();
                        tabelsLayout.insertInitialTableValues();
                        panelPrepare(true);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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

                        pdf.setVisible(false);

                        Resource resource = new ExternalResource(fedoraPath + currentSelectedArticle.getId() + "/datastreams/XML/content");
                        link.setResource(resource);
                        link.setDescription("Link to Second Page");
                    } else if ("PAGE".equals(itemClickEvent.getComponent().getId())) {
                        currentSelectedPage = (Page) itemClickEvent.getItemId();
                        currentSelectedArticle = null;

                        pdf.setVisible(true);
                        String path = model.getItemFromUuid(currentSelectedPage.getId()).getPath();
                        final URI uri = new URI(null, null, bitRepoPath + path + ".pdf", null);
                        StreamResource streamRecource = createStreamResource(uri);
                        pdf.setSource(streamRecource);
                        Resource resource = new ExternalResource(fedoraPath + currentSelectedPage.getId() + "/datastreams/XML/content");
                        link.setResource(resource);
                        link.setDescription("Link to Second Page");
                    }


                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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
        viewControlLayout.addComponent(link);

        viewLayout.addComponent(viewControlLayout);
        viewLayout.addComponent(pdf);

        mainhlayout.addComponent(viewLayout);
        layout.addComponent(mainhlayout);
    }

    private void panelPrepare(boolean prepare) {
        pdf.setVisible(prepare);
        link.setVisible(prepare);
        tabelsLayout.setVisible(prepare);
    }

    /**
     * Convert the URI to the pdf-file into a StremaRecource for viewing in UI
     * @param uri
     * @return
     * @throws Exception
     */
    private synchronized StreamResource createStreamResource(final URI uri) throws Exception {

        final StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                try {
                    InputStream inps = uri.toURL().openStream();
                    return inps;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return null;
                }
            }
        }, "pages.pdf"  );
        resource.setMIMEType("application/pdf");
        resource.setCacheTime(1000);
        return resource;
    }

    /**
     * Show a welcome message when entering pte page
     * @param event
     */
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Notification.show("DPA Delivery validation");
    }

}