package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import com.google.common.base.Charsets;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.ToolResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf.ValidationResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf.ValidationResults;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf.VeraPDFOutputValidation;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf.VeraPDFValidator;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.EventTrigger;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.inject.Named;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.VeraPDFInvokeMain.VERAPDF_DATASTREAM_NAME;

/**
 * Unfinished
 */
public class VeraPDFAnalyzeMain {
    protected static final Logger log = LoggerFactory.getLogger(VeraPDFAnalyzeMain.class);

    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerVeraPDFAnalyzeMain_VeraPdfTaskDaggerComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, VeraPDFAnalyzeModule.class, BitRepositoryModule.class})
    interface VeraPdfTaskDaggerComponent {
        Tool getTool();
    }

    @Module
    public static class VeraPDFAnalyzeModule {

        public static final String VERAPDF_INVOKED = "VeraPDF_Invoked";
        public static final String VERAPDF_ANALYZED = "VeraPDF_Analyzed";
        public static final String DPA_VERAPDF_FLAVOR = "dpa.verapdf.flavor";
        public static final String DPA_VERAPDF_REUSEEXISTINGDATASTREAM = "dpa.verapdf.reuseexistingdatastream";
        public static final String VERAPDF_ANALYSIS_DATASTREAM_NAME = "VERAPDF_ANALYSIS";

        @Provides
//        Runnable provideRunnable(Modified_SBOIEventIndex index, DomsEventStorage<Item> domsEventStorage, Stream<EventTrigger.Query> queryStream, Task task) {
        protected Tool provideTool(QuerySpecification query, DomsRepository domsRepository,
                                   EnhancedFedora efedora, DomsEventStorage<Item> domsEventStorage,
                                   VeraPDFOutputValidation veraPDFOutputValidation) {

            Tool f = () -> Stream.of(query)
                    .flatMap(domsRepository::query)
                    .peek(o -> log.trace("Query returned: {}", o))
                    .map(domsItem -> processChildDomsId(domsRepository, domsEventStorage, veraPDFOutputValidation).apply(domsItem))
                    // Collect results for each domsId
                    .peek(o -> log.trace("Result: {}", o))
                    .collect(Collectors.toList())
                    .toString();

            return f;
        }

        private Function<DomsItem, String> processChildDomsId(DomsRepository domsRepository, DomsEventStorage<Item> domsEventStorage, VeraPDFOutputValidation veraPDFOutputValidation) {
            return domsItem -> {
                long startTime = System.currentTimeMillis();

                // Single doms item
                List<ToolResult> toolResults = domsItem.allChildren().stream()
                        .flatMap(childDomsItem -> analyzeVeraPDFDataStream(childDomsItem, veraPDFOutputValidation))
                        .collect(Collectors.toList());

                // Sort according to result
                final Map<Boolean, List<ToolResult>> toolResultMap = toolResults.stream()
                        .collect(Collectors.groupingBy(tr -> tr.getResult()));

                List<ToolResult> failingToolResults = toolResultMap.getOrDefault(Boolean.FALSE, Collections.emptyList());

                String deliveryEventMessage = failingToolResults.stream()
                        .map(tr -> "---\n" + tr.getHumanlyReadableMessage() + "\n" + tr.getHumanlyReadableStackTrace())
                        .filter(s -> s.trim().length() > 0) // skip blank lines
                        .collect(Collectors.joining("\n"));

                // outcome was successful only if no toolResults has a FALSE result.
                boolean outcome = failingToolResults.size() == 0;

                final String keyword = getClass().getSimpleName();
                final Date timestamp = new Date();

                domsItem.appendEvent(keyword, timestamp, deliveryEventMessage, VERAPDF_INVOKED, outcome);

                log.info("{} {} Took: {} ms", keyword, domsItem, (System.currentTimeMillis() - startTime));
                return domsItem + " processed. " + failingToolResults.size() + " failed. outcome = " + outcome;
            };
        }

        protected Stream<ToolResult> analyzeVeraPDFDataStream(DomsItem domsItem, VeraPDFOutputValidation veraPDFOutputValidation) {

            final List<DomsDatastream> datastreams = domsItem.datastreams();

            Optional<DomsDatastream> profileOptional = datastreams.stream()
                    .filter(ds -> ds.getID().equals(VERAPDF_DATASTREAM_NAME))
                    .findAny();

            if (profileOptional.isPresent() == false) {
                return Stream.of();
            }

            log.trace("analyzing {}", domsItem);

            DomsDatastream ds = profileOptional.get();

            // FIXME: only exposed datastream loading method returns a string,
            // we have ascii only so we can do some naughty assumptions about converting
            // the strings back to a byte stream and load it.

            String datastreamString = ds.getDatastreamAsString();
            //log.trace("datastream value: {}", datastreamString);
            InputStream characterConversionTroubledDatastreamInputStream =
                    new ByteArrayInputStream(datastreamString.getBytes(Charsets.UTF_8));

            // XML datastream bytes loaded.

            List<String> rejected;
            try {
                rejected = veraPDFOutputValidation.extractRejected(characterConversionTroubledDatastreamInputStream);
            } catch (ParserConfigurationException | IOException | SAXException | XPathExpressionException e) {
                log.trace("failed to extract rejected", e);
                return Stream.of(ToolResult.fail("extractRejected(..) on " + domsItem));
            }
            ValidationResults l = veraPDFOutputValidation.validateResult(rejected);

            Map<ValidationResult.ValidationResultEnum, List<String>> severenessMap = new TreeSet<>(rejected).stream()
                    .collect(Collectors.groupingBy(veraPDFOutputValidation::severenessFor));

            String analysisResult = severenessMap.entrySet().stream().
                    map(Object::toString)
                    .collect(Collectors.joining("\n"));

            final ValidationResult.ValidationResultEnum worstBrokenRule = l.getWorstBrokenRule();

            // Save analysis in datastream.
            String comment = getClass().getCanonicalName();

            try {
                domsItem.modifyDatastreamByValue(
                        VERAPDF_ANALYSIS_DATASTREAM_NAME,
                        null, // no checksum
                        null, // no checksum
                        analysisResult.getBytes(Charsets.UTF_8),
                        null,
                        "text/plain",
                        comment,
                        null);
            } catch (Exception e) {
                return Stream.of(ToolResult.fail(domsItem + " could not save to datastream"));
            }

            switch (worstBrokenRule) {
                case INVALID:
                    return Stream.of(ToolResult.fail(domsItem + " " + worstBrokenRule));
                case UNKNOWN:
                    return Stream.of(ToolResult.fail(domsItem + " " + worstBrokenRule));
                case MANUAL_INSPECTION:
                    return Stream.of(ToolResult.fail(domsItem + " " + worstBrokenRule));
                case ACCEPTABLE:
                    return Stream.of(ToolResult.ok(domsItem + " " + worstBrokenRule));
                default:
                    throw new RuntimeException("Unexpected worst rule" + worstBrokenRule);
            }
        }

        @Provides
        protected Function<QuerySpecification, Stream<DomsId>> sboiEventIndexSearch(SBOIEventIndex<Item> index) {
            return query -> sboiEventIndexSearch(query, index).stream();
        }

        private List<DomsId> sboiEventIndexSearch(QuerySpecification query, SBOIEventIndex<Item> index) {
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
        @Named("pageSize")
        Integer providePageSize(ConfigurationMap map) {
            return Integer.valueOf(map.getRequired("pageSize"));
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }

        @Provides
        @Named(DPA_VERAPDF_FLAVOR)
        String getVeraPDFFlavor(ConfigurationMap map) {
            return map.getRequired(DPA_VERAPDF_FLAVOR);
        }

        @Provides
        Function<InputStream, byte[]> getVeraPDFInvoker(@Named(DPA_VERAPDF_FLAVOR) String flavorId) {
            return new VeraPDFValidator(flavorId, true);
        }

        @Provides
        @Named(DPA_VERAPDF_REUSEEXISTINGDATASTREAM)
        boolean provideReuseExistingDatastream(ConfigurationMap map) {
            return Boolean.valueOf(map.getDefault(DPA_VERAPDF_REUSEEXISTINGDATASTREAM, "false"));
        }

        @Provides
        VeraPDFOutputValidation provideVeraPDFOutputValidation() {
            return new VeraPDFOutputValidation(true);
        }
    }
}