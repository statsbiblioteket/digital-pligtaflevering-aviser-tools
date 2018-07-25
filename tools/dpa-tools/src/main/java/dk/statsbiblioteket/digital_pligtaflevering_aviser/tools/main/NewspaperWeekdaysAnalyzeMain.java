package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.EventQuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.DeliveryPattern;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.WeekdayResult;
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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.IngesterMain.DPA_DELIVERIES_FOLDER;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
                                   @Named(DPA_DELIVERY_PATTERN_XML_PATH) String deliveryXmlPath,
                                   DefaultToolMXBean mxBean) {

            final String agent = getClass().getSimpleName();
    
            DeliveryPattern deliveryPattern;
            try (InputStream is = new FileInputStream(deliveryXmlPath);) {
                JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryPattern.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                deliveryPattern = (DeliveryPattern) jaxbUnmarshaller.unmarshal(is);
            } catch (Exception e){
                throw new RuntimeException("Failed to load deliveryPattern from '"+deliveryXmlPath+"'",e);
            }
            

            Tool f = () -> {
                List<DomsItem> roundtripItems = Stream.of(workToDoQuery)
                        .flatMap(domsRepository::query)
                        .peek(o -> log.trace("Query returned: {}", o))
                        .collect(toList());

                Pattern pattern = Pattern.compile("dl_(........)_rt.*");

                List<String> result = new ArrayList<>();

                for (DomsItem item : roundtripItems) {
                    String relativePath = item.getPath();

                    Path deliveryPath = Paths.get(deliveriesFolder, relativePath).toAbsolutePath();

                    mxBean.currentId = deliveryPath.toString();
                    mxBean.idsProcessed++;

                    if (Files.exists(deliveryPath) == false) {
                        throw new RuntimeException(new FileNotFoundException(deliveryPath.toString()));
                    }

                    // Same as ingester
                    Set<String> foundTitles = Files.walk(deliveryPath, 1)
                            .filter(Files::isDirectory)
                            .skip(1) // Skip the parent directory itself.  FIXME:  Ensure well-definedness
                            .map(p -> p.getFileName().toString())
                            .collect(toSet());

                    Matcher matcher = pattern.matcher(relativePath);
                    if (matcher.matches() == false) {
                        continue;
                    }
                    String dateString = matcher.group(1);
                    LocalDate localDate = LocalDate.parse(dateString.substring(0, 4) + "-" + dateString.substring(4, 6) + "-" + dateString.substring(6, 8));
                    DayOfWeek dayOfWeek = localDate.getDayOfWeek();

                    String[] dayIds = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                    String dayId = dayIds[dayOfWeek.getValue() - 1];

                    Set<String> expectedTitles = deliveryPattern.entryStream()
                            .filter(e -> e.getValue().getDayState(dayId))
                            .map(e -> e.getKey())
                            .sorted()
                            .collect(toSet());

                    Set<String> missingTitles = new HashSet<>(expectedTitles);
                    missingTitles.removeAll(foundTitles);

                    Set<String> extraTitles = new HashSet<>(foundTitles);
                    extraTitles.removeAll(expectedTitles);

                    
                    
                    /*  Now generate this snippet:
                    {
                        "weekday": "Mon",
                        "expectedTitles": [
                            "avis1",
                            "avis2"
                         ],
                        "foundTitles": [
                            "avis1",
                            "avis3"
                         ],
                        "missingTitles":[
                            "avis2"
                         ],
                        "extraTitles":[
                            "avis3"
                        ]
                    }
                     */
    
    
                    WeekdayResult weekDayResult = new WeekdayResult(dayId,
                                                                    new ArrayList<>(expectedTitles),
                                                                    new ArrayList<>(foundTitles),
                                                                    new ArrayList<>(missingTitles),
                                                                    new ArrayList<>(extraTitles));
    
                    ObjectMapper mapper = new ObjectMapper()
                            .enable(SerializationFeature.INDENT_OUTPUT)
                            .enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
    
                    DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
                    prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    
                    String jsonSnippet = mapper.writer(prettyPrinter).writeValueAsString(weekDayResult);
    
                    item.modifyDatastreamByValue("NEWSPAPERWEEKDAY",
                                                 null,
                                                 null,
                                                 jsonSnippet.getBytes(StandardCharsets.UTF_8),
                                                 null,
                                                 "text/plain",
                                                 null,
                                                 null);
                    item.appendEvent(new DomsEvent(agent, new Date(), jsonSnippet, eventName, true));


                    result.add(item.getDomsId().id() + " " + deliveryPath + " missingTitles: " + jsonifySet(missingTitles));
                }
                return result;
            };
            return f;
        }

        public String jsonifySet(Set<String> set) {
            return "[" +
                    set.stream()
                            .sorted()
                            .map(s -> "\"" + s.replaceAll("\"", "\\") + "\"")
                            .collect(joining(", "))
                    + "]";
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
