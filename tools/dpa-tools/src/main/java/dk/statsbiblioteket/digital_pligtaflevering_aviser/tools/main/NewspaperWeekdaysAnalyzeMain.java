package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.kb.stream.StreamTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.EventQuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.EventTrigger;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.IngesterMain.DPA_DELIVERIES_FOLDER;
import static java.util.stream.Collectors.toList;

/**
 * Unfinished
 */
public class NewspaperWeekdaysAnalyzeMain {
    protected static final Logger log = LoggerFactory.getLogger(NewspaperWeekdaysAnalyzeMain.class);

    public static final String DPA_DELIVERY_PATTERN_XML_PATH = "dpa.delivery-pattern-xml.path";

    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerNewspaperWeekdaysAnalyzeMain_NewspaperWeekdaysAnalyzeDaggerComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, NewspaperWeekdaysAnalyzeModule.class})
    interface NewspaperWeekdaysAnalyzeDaggerComponent {
        Tool getTool();

    }

    /**
     * @noinspection WeakerAccess
     */
    @Module
    public static class NewspaperWeekdaysAnalyzeModule {

        /**
         * @noinspection PointlessBooleanExpression
         */
        @Provides
        protected Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                                   QuerySpecification workToDoQuery,
                                   DomsRepository domsRepository,
                                   EnhancedFedora efedora,
                                   DomsEventStorage<Item> domsEventStorage,
                                   @Named(DPA_DELIVERIES_FOLDER) String deliveriesFolder,
                                   @Named(DPA_DELIVERY_PATTERN_XML_PATH) String deliveryXmlPath, // DeliveryPattern.xml - talk to mmj.
                                   DefaultToolMXBean mxBean) {

            final String agent = getClass().getSimpleName();

            
            InputStream is = new FileInputStream(deliveryXmlPath); //
            JAXBContext jaxbContext1 = JAXBContext.newInstance(DeliveryPattern.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
            DeliveryPattern deserializedObject = (DeliveryPattern) jaxbUnmarshaller.unmarshal(is);

            WeekPattern deliveryInfo = deserializedObject.getWeekPattern("viborgstiftsfolkeblad");
            assertEquals(deliveryInfo.getDayState("Mon"), TRUE);
            assertEquals(deliveryInfo.getDayState("Tue"), TRUE);
            assertEquals(deliveryInfo.getDayState("Wed"), TRUE);
            assertEquals(deliveryInfo.getDayState("Thu"), TRUE);
            assertEquals(deliveryInfo.getDayState("Fri"), TRUE);
            assertEquals(deliveryInfo.getDayState("Sat"), TRUE);
            assertEquals(deliveryInfo.getDayState("Sun"), FALSE);



            Tool f = () -> Stream.of(workToDoQuery)
                    .flatMap(domsRepository::query)
                    .peek(o -> log.trace("Query returned: {}", o))

                    .map(item -> new StreamTuple<>(item, Paths.get(deliveriesFolder, item.getPath())))

//                    .map(st -> st.map(roundtripItem -> {
//                        Map<Boolean, List<Either<Throwable, Boolean>>> eithersFromRoundtrip = roundtripItem.children()
//                                .peek(i -> mxBean.currentId = String.valueOf(i))
//                                .peek(i -> mxBean.idsProcessed++)
//                                // ignore those already processed if flag is set
//                                .filter(i -> (i.datastreams().stream()
//                                        .anyMatch(ds -> ds.getId().equals(VERAPDF_DATASTREAM_NAME)) && reuseExistingDatastream) == false)
//                                // process pdf datastreams.
//                                .flatMap(i -> i.datastreams().stream()
//                                        .filter(datastream -> datastream.getMimeType().equals("application/pdf"))
//                                        .peek(datastream -> log.trace("Found PDF stream on {}", i))
//                                        .map(datastream -> getUrlForBitrepositoryItemPossiblyLocallyAvailable(i, bitrepositoryURLPrefix, bitrepositoryMountpoint, datastream.getUrl()))
//                                        .map(url -> Try.of(() -> {
//                                            log.trace("{} - processing URL: {}", i, url);
//                                            long startTime = System.currentTimeMillis();
//                                            String veraPDF_output = veraPdfInvokerProvider.get().apply(url);
//                                            i.modifyDatastreamByValue(VERAPDF_DATASTREAM_NAME, null, null, veraPDF_output.getBytes(StandardCharsets.UTF_8), null, "text/xml", "URL: " + url, null);
//                                            log.info(KibanaLoggingStrings.FINISHED_FILE_PDFINVOKE, url, (System.currentTimeMillis() - startTime));
//                                            return Boolean.TRUE;
//                                        }).toEither())
//                                        .peek((Either<Throwable, Boolean> s) -> log.trace("{}", s)))
//                                .collect(partitioningBy(either -> either.isRight()));
//
//                        List<Boolean> successful = eithersFromRoundtrip.get(Boolean.TRUE).stream()
//                                .map(either -> either.right().get())
//                                .collect(toList());
//                        List<String> failed = eithersFromRoundtrip.get(Boolean.FALSE).stream()
//                                .map(either -> DomsItemTuple.stacktraceFor(either.left().get()))
//                                .collect(toList());
//
//                        if (failed.size() == 0) {
//                            roundtripItem.appendEvent(new DomsEvent(agent, new Date(), successful.size() + " processed", eventName, true));
//                            return true;
//                        } else {
//                            // we have encountered exceptions not handled lower down.  Report those.
//                            roundtripItem.appendEvent(new DomsEvent(agent, new Date(), String.join("\n\n", failed), eventName, false));
//                            return false;
//                        }
//                    }))
                    .collect(toList());

            return f;
        }

        @Provides
        protected Function<EventQuerySpecification, Stream<DomsId>> sboiEventIndexSearch(SBOIEventIndex<Item> index) {
            return query -> sboiEventIndexSearch(query, index).stream();
        }

        private List<DomsId> sboiEventIndexSearch(EventQuerySpecification query, SBOIEventIndex<Item> index) {
            Iterator<Item> iterator;
            try {
                EventTrigger.Query<Item> q = new EventTrigger.Query<>();
                q.getPastSuccessfulEvents().addAll(query.getPastSuccessfulEvents());
                q.getOldEvents().addAll(query.getOldEvents());
                q.getFutureEvents().addAll(query.getFutureEvents());
                q.getTypes().addAll(query.getTypes());
                iterator = index.search(false, q);
            } catch (CommunicationException e) {
                throw new RuntimeException("sboiEventIndexSearch()", e);
            }
            // http://stackoverflow.com/a/28491752/53897
            // To keep this simple we simply read in the whole result in a list.
            List<DomsId> l = new ArrayList<>();
            iterator.forEachRemaining(item -> l.add(new DomsId(item.getDomsID())));
            return l;
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }

        /**
         * This is the folder have been put so we can locate the files corresponding to the trigger.
         *
         * @param map configuration map
         * @return
         */
        @Provides
        @Produces
        @Named(DPA_DELIVERIES_FOLDER)
        String provideDeliveriesFolder(ConfigurationMap map) {
            return map.getRequired(DPA_DELIVERIES_FOLDER);
        }

        @Provides
        @Named(DPA_DELIVERY_PATTERN_XML_PATH)
        String provideManualControlConfigPath(ConfigurationMap map) {
            return map.getRequired(DPA_DELIVERY_PATTERN_XML_PATH);
        }
    }
}
