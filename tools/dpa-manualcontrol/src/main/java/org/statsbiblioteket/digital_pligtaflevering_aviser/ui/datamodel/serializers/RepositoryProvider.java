package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers;

import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex_DigitalPligtafleveringAviser;
import io.vavr.control.Try;

import java.util.function.Function;

/**
 * RepositoryProvider captures the Dagger configuration into code so a DomsRepository can
 * be correctly configured from a ConfigurationMap configuration.
 * This piece of code is copied directly from tools/dpa-dashboards/src/main/java/RepositoryConfigurator
 */
public class RepositoryProvider implements Function<ConfigurationMap, DomsRepository> {

    @Override
    public DomsRepository apply(ConfigurationMap map) {

        DomsModule domsModule = new DomsModule();

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
        int pageSize = domsModule.providePageSize(map);

        final String recordBase = domsModule.provideDomsCollection(map);

        SBOIEventIndex_DigitalPligtafleveringAviser sboiEventIndex = Try.of(
                () -> new SBOIEventIndex_DigitalPligtafleveringAviser(summaLocation, premisManipulatorFactory, domsEventStorage, pageSize, recordBase)
        ).get();
        WebResource webResource = domsModule.provideConfiguredFedoraWebResource(domsURL, domsUserName, domsPassword);

        DomsRepository repository = new DomsRepository(sboiEventIndex, webResource, efedora, domsEventStorage, summaLocation, recordBase);
        return repository;
    }
}
