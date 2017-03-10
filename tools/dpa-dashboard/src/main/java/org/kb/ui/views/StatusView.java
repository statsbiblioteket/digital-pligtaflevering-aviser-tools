package org.kb.ui.views;

import com.sun.jersey.api.client.WebResource;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import org.kb.ui.DataModel;
import org.kb.ui.panels.EventPanel;
import org.kb.ui.FetchEventStructure;
import org.kb.ui.panels.InfoPanel;
import org.kb.ui.panels.SearchPanel;
import org.kb.ui.panels.StatePanel;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by mmj on 3/8/17.
 */
public class StatusView extends VerticalLayout implements View {

    private FetchEventStructure eventStructureCommunication = new FetchEventStructure();
    private DataModel model = new DataModel();

    public StatusView() {

        final VerticalLayout layout = new VerticalLayout();
        final HorizontalLayout hlayout = new HorizontalLayout();
        hlayout.setWidth("100%");
        layout.setMargin(true);
        addComponent(layout);

        StatePanel table = new StatePanel();
        table.setSelectable(true);
        table.setImmediate(true);

        InfoPanel infoPanel = new InfoPanel();
        infoPanel.setVisible(false);
        infoPanel.setSelectable(true);
        infoPanel.setImmediate(true);

        EventPanel eventPanel = new EventPanel();
        eventPanel.setVisible(false);

        SearchPanel button = new SearchPanel();
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                ArrayList<String> alist = new ArrayList<String>();

                Stream<DomsItem> list = fetchIt();

                list.forEach(new Consumer<DomsItem>() {

                    @Override
                    public void accept(final DomsItem o) {
                        alist.add(o.getPath());
                    }
                });

                table.readStates(eventStructureCommunication);
            }
        });


        table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                infoPanel.setVisible(true);
                String currentEvent = (String)(itemClickEvent.getItem().getItemProperty("State").getValue());
                infoPanel.setInfo(eventStructureCommunication, currentEvent);
                model.setCurrentEvent(currentEvent);
            }
        });


        infoPanel.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                eventPanel.setVisible(true);

                String heading = (String)(itemClickEvent.getItem().getItemProperty("Batch").getValue());

                eventPanel.setInfo(eventStructureCommunication, model.getCurrentEvent(), heading);
            }
        });

        layout.addComponent(button);

        hlayout.addComponent(table);
        hlayout.addComponent(infoPanel);
        hlayout.addComponent(eventPanel);
        layout.addComponent(hlayout);
    }


    private Stream<DomsItem> fetchIt() {

        Stream<DomsItem> r = null;

        try {

            ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties("/home/mmj/projects/digital-pligtaflevering-aviser-tools/tools/dpa-tools-ide-launchers/src/main/resources/xmlvalidate-vagrant.properties");

            CommonModule commonModule = new CommonModule();
            DomsModule domsModule = new DomsModule();
            BitRepositoryModule bitRepositoryModule = new BitRepositoryModule();

            String domsUserName = domsModule.provideDomsUserName(map);
            String domsPassword = domsModule.provideDomsPassword(map);
            final String domsURL = domsModule.provideDomsURL(map);
            String fedoraLocation = domsModule.provideDomsURL(map);
            String domsPidgeneratorUrl = domsModule.provideDomsPidGeneratorURL(map);

            int fedoraRetries = domsModule.getFedoraRetries(map);
            int fedoraDelayBetweenRetries = domsModule.getFedoraDelayBetweenRetries(map);

            EnhancedFedora efedora = domsModule.provideEnhancedFedora(domsUserName, domsPassword, fedoraLocation, domsPidgeneratorUrl, fedoraRetries, fedoraDelayBetweenRetries);

            ItemFactory<Item> itemFactory = new ItemFactory<Item>() {
                @Override
                public Item create(String id) {
                    return new Item();
                }
            };

            String summaLocation = domsModule.provideSummaLocation(map);
            PremisManipulatorFactory<Item> premisManipulatorFactory = domsModule.providePremisManipulatorFactory(itemFactory);

            DomsEventStorage<Item> domsEventStorage = domsModule.provideDomsEventStorage(domsURL, domsPidgeneratorUrl, domsUserName, domsPassword, itemFactory);
            int pageSize = 10;//domsModule.providePageSize(map);

            SBOIEventIndex sboiEventIndex = new SBOIEventIndex(summaLocation, premisManipulatorFactory, domsEventStorage, pageSize);
            WebResource webResource = domsModule.provideConfiguredFedoraWebResource(domsURL, domsUserName, domsPassword);

            DomsRepository repository = new DomsRepository(sboiEventIndex, webResource, efedora, domsEventStorage);

            String pastSuccessfulEvents = domsModule.providePastSuccesfulEvents(map);
            String futureEvents = domsModule.provideFutureEvents(map);
            String oldEvents = domsModule.provideOldEvents(map);
            String itemTypes = domsModule.provideItemTypes(map);

            final QuerySpecification querySpecification = domsModule.providesQuerySpecification(pastSuccessfulEvents, futureEvents, oldEvents, itemTypes);
            r = repository.query(querySpecification);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return r;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Notification.show("Welcome to Status");
    }

}