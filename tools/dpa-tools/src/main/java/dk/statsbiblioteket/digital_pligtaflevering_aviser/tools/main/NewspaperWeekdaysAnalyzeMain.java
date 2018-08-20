package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
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
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.DeliveryPatternList;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.WeekPattern;
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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.InvalidObjectException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.GenerateStatisticsMain.STATISTICS_STREAM_NAME;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.IngesterMain.DPA_DELIVERIES_FOLDER;
import static java.util.stream.Collectors.toList;

/**
 * Unfinished. -TBA
 *
 * You can say that again.... -ABR
 */
public class NewspaperWeekdaysAnalyzeMain {
    protected static final Logger log = LoggerFactory.getLogger(NewspaperWeekdaysAnalyzeMain.class);

    //The path for the xml-file describing the deliverypattern, of the form path:lastDeliveryWithThisPattern,path:astDeliveryWithThisPattern
    public static final String DPA_DELIVERY_PATTERN = "dpa.delivery-pattern";
    
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

      
        @Provides
        protected Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                                   QuerySpecification workToDoQuery,
                                   DomsRepository domsRepository,
                                   EnhancedFedora efedora,
                                   DomsEventStorage<Item> domsEventStorage,
                                   @Named(DPA_DELIVERIES_FOLDER) String deliveriesFolder,
                                   DeliveryPatternList deliveryPatternList,
                                   DefaultToolMXBean mxBean) {

            final String agent = getClass().getSimpleName();
    
            ObjectWriter writer = createJsonWriter();
    
            //Pattern pattern = Pattern.compile("dl_(\\d{4})-(\\d{2})-(\\d{2})_rt\\d+");
            Pattern pattern = Pattern.compile("dl_([\\d]{8})_rt\\d+");
            
            Tool tool = () -> {
                //Find the roundtrips
                List<DomsItem> roundtripItems = Stream.of(workToDoQuery)
                        .flatMap(domsRepository::query)
                        .peek(o -> log.trace("Query returned: {}", o))
                        .collect(toList());

              
                List<String> result = new ArrayList<>();

                //For each round trip
                for (DomsItem item : roundtripItems) {
    
                    mxBean.currentId = item.toString();
                    mxBean.idsProcessed++;
    
                    //Get the roundtrip name of the form dl_(........)_rt.*
                    String relativePath = item.getPath();
                    
                    //Calculate the day-name from the delivery date
                    DayOfWeek dayId;
    
                    try {
                        dayId = getDayID(pattern,relativePath);
                    } catch (InvalidObjectException e) {
                        log.warn("Could not parse delivery date from delivery {} with name '{}', so skipping it",
                                 item,
                                 relativePath);
                        continue;
                    }
    
                    DeliveryPattern deliveryPattern = deliveryPatternList.getMostRecentDeliveryPatternBefore(
                            relativePath);

                    Stream<Map.Entry<String, WeekPattern>> wp = deliveryPattern.entryStream();

                    List<String> expectedTitles = wp.filter(e -> e.getValue().getDayState(dayId))
                            .map(e -> e.getKey())
                            .distinct()
                            .sorted()
                            .collect(toList());

                    List<String> foundTitles = new ArrayList<String>();

                    //Start initializing datamodel of tileObjects that has not yet been performed
                    final List<DomsDatastream> datastreams = item.datastreams();
                    Optional<DomsDatastream> profileOptional = datastreams.stream()
                            .filter(ds -> ds.getId().equals(STATISTICS_STREAM_NAME))
                            .findAny();

                    //<deliveryStatistics deliveryName="dl_20160811_rt1"><titles><title titleName="verapdf"><pages><page checkedState="UNCHECKED" id="uuid:f1642d44-fe50-441e-bedb-99562a034353" pageName="dl_20160811_rt1/verapdf/pages/20160811_verapdf_section01_page005_v20160811x1#0005" pageNumber="1" sectionName="undefined" sectionNumber="1"/>
                    if (profileOptional.isPresent()) {

                        DomsDatastream ds = profileOptional.get();
                        //We are reading this textstring as a String and are aware that this might lead to encoding problems
                        StringReader reader = new StringReader(ds.getDatastreamAsString());
                        InputSource inps = new InputSource(reader);

                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document doc = builder.parse(inps);
                        XPathFactory xPathfactory = XPathFactory.newInstance();
                        XPath xpath = xPathfactory.newXPath();
                        XPathExpression expr = xpath.compile("/deliveryStatistics/*[local-name()='titles']/*[local-name()='title']/@titleName");
                        NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                        for (int i = 0; i < nl.getLength(); i++) {
                            String titleItem = nl.item(i).getNodeValue();
                            XPathExpression pagesExpression = xpath.compile("/deliveryStatistics/*[local-name()='titles']/*[local-name()='title'][@titleName='" + titleItem + "']/*[local-name()='pages']/*[local-name()='page']");
                            NodeList pages = (NodeList) pagesExpression.evaluate(doc, XPathConstants.NODESET);
                            if(pages.getLength()>0) {
                                foundTitles.add(titleItem);
                            }

                        }
                    }

                    //Missing titles must be ExpectedTitles - FoundTitles
                    List<String> missingTitles = new ArrayList<>(expectedTitles);
                    missingTitles.removeAll(foundTitles);

                    //Extra titles must be FoundTitles - ExpectedTitles
                    List<String> extraTitles = new ArrayList<>(foundTitles);
                    extraTitles.removeAll(expectedTitles);

                    WeekdayResult weekDayResult = new WeekdayResult(dayId.getDisplayName(TextStyle.SHORT, Locale.US),
                                                                    expectedTitles,
                                                                    foundTitles,
                                                                    missingTitles,
                                                                    extraTitles);
    
    
                    String jsonSnippet = writer.writeValueAsString(weekDayResult);

                    boolean failState;
                    failState = weekDayResult.getMissingTitles().size() <= 0;
    
                    item.modifyDatastreamByValue("NEWSPAPERWEEKDAY",
                                                 null,
                                                 null,
                                                 jsonSnippet.getBytes(StandardCharsets.UTF_8),
                                                 null,
                                                 "text/plain",
                                                 null,
                                                 null);
                    item.appendEvent(new DomsEvent(agent, new Date(), jsonSnippet, eventName, failState));


                    result.add(item.getDomsId() + " missingTitles: " + writer.writeValueAsString(missingTitles));
                }
                return result;
            };
            return tool;
        }
    
    
        /**
         *
         *                       Now generate this snippet:
         *                     {
         *                         "weekday": "Mon",
         *                         "expectedTitles": [
         *                             "avis1",
         *                             "avis2"
         *                          ],
         *                         "foundTitles": [
         *                             "avis1",
         *                             "avis3"
         *                          ],
         *                         "missingTitles":[
         *                             "avis2"
         *                          ],
         *                         "extraTitles":[
         *                             "avis3"
         *                         ]
         *                     }
         *
         **/
        protected ObjectWriter createJsonWriter() {
            ObjectMapper mapper = new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        
            DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
            prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        
            return mapper.writer(prettyPrinter);
        }
    
        /**
         * Get the weekday name from the delivery name.
         *
         * Parses the deliveryName as a date, and calculates the weekday name from that
         * @param pattern
         * @param deliveryName
         * @return
         * @throws InvalidObjectException
         */
        protected DayOfWeek getDayID(Pattern pattern, String deliveryName) throws InvalidObjectException {
            //If the item is not a delivery, skip it
            Matcher matcher = pattern.matcher(deliveryName);
            if (matcher.matches() == false) {
                throw new InvalidObjectException("deliveryName '"+deliveryName+"' does not match pattern '"+pattern+"'");
            }
            
            String dateString = matcher.group(1);
            DayOfWeek dayOfWeek = LocalDate.parse(dateString,
                                                  new DateTimeFormatterBuilder().appendPattern("yyyyMMdd").toFormatter())
                                           .getDayOfWeek();
        
            
            return dayOfWeek;
        }


        protected LocalDate getDate(Pattern pattern, String deliveryName) throws InvalidObjectException {
            //If the item is not a delivery, skip it
            Matcher matcher = pattern.matcher(deliveryName);
            if (matcher.matches() == false) {
                throw new InvalidObjectException("deliveryName '"+deliveryName+"' does not match pattern '"+pattern+"'");
            }

            String dateString = matcher.group(1);
            return LocalDate.parse(dateString,
                    new DateTimeFormatterBuilder().appendPattern("yyyyMMdd").toFormatter());
        }
    
        @Provides
        protected DeliveryPatternList getDeliveryPattern(@Named(DPA_DELIVERY_PATTERN) String deliveryPatterns) {
            return DeliveryPatternList.parseFromString(deliveryPatterns);
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
        @Named(DPA_DELIVERY_PATTERN)
        String provideManualControlConfigPath1(ConfigurationMap map) {
            return map.getRequired(DPA_DELIVERY_PATTERN);
        }


    }
}
