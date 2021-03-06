package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.DeliveryPatternList;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.DomsItemTuple;
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
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.io.InputStream;
import java.io.InvalidObjectException;
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
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.GenerateStatisticsMain.STATISTICS_STREAM_NAME;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.IngesterMain.DPA_DELIVERIES_FOLDER;
import static java.util.stream.Collectors.toList;


public class ManualQACompleterMain {
    protected static final Logger log = LoggerFactory.getLogger(ManualQACompleterMain.class);
    
    //The path for the xml-file describing the deliverypattern, of the form path:lastDeliveryWithThisPattern,path:astDeliveryWithThisPattern
    public static final String DPA_DELIVERY_PATTERN = "dpa.delivery-pattern";
    
    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerManualQACompleterMain_ManualQACompleterMainDaggerComponent.builder()
                                                                                     .configurationMap(m)
                                                                                     .build()
                                                                                     .getTool()
        );
    }
    
    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, ManualQACompleterModule.class})
    interface ManualQACompleterMainDaggerComponent {
        Tool getTool();
        
    }
    
    /**
     * @noinspection WeakerAccess
     */
    @Module
    public static class ManualQACompleterModule {
        
        
        @Provides
        protected Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                                   QuerySpecification workToDoQuery,
                                   DomsRepository domsRepository,
                                   EnhancedFedora efedora,
                                   DomsEventStorage<Item> domsEventStorage,
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
    
                    //item.children are the newspapers
                    
                    
                    //Newspapers are item.children()
    
    
                    //if any of the newspapers do not have a VALIDATIONINFO datastream
                    if (item.children().anyMatch(newspaper -> newspaper.datastreams()
                                                                       .stream()
                                                                       .noneMatch(datastream -> "VALIDATIONINFO".equals(
                                                                               datastream.getId())))) {
                        continue;
                    }
    
    
                    //if any of the newspaper have a validationinfo datastream that is not checked
                    XPathSelector xpath = DOM.createXPathSelector("ns", "kb.dk/dpa/delivery-statistics");
                    if (item.children().anyMatch(newspaper -> {
                        Document doc = DOM.stringToDOM(newspaper.datastream("VALIDATIONINFO").getDatastreamAsString(),
                                                       true);
                        return !xpath.selectBoolean(doc, "/deliveryTitleInfo/@checked");
                    })) {
                        continue;
                    }
                    
                    
                    item.appendEvent(new DomsEvent(agent, new Date(), "", eventName, true));
    
    
                    result.add(item.getDomsId().toString());
                }
                return result;
            };
            return tool;
        }
        
        
        /**
         * Now generate this snippet:
         * {
         * "weekday": "Mon",
         * "expectedTitles": [
         * "avis1",
         * "avis2"
         * ],
         * "foundTitles": [
         * "avis1",
         * "avis3"
         * ],
         * "missingTitles":[
         * "avis2"
         * ],
         * "extraTitles":[
         * "avis3"
         * ]
         * }
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
         * <p>
         * Parses the deliveryName as a date, and calculates the weekday name from that
         *
         * @param pattern
         * @param deliveryName
         * @return
         * @throws InvalidObjectException
         */
        protected DayOfWeek getDayID(Pattern pattern, String deliveryName) throws InvalidObjectException {
            //If the item is not a delivery, skip it
            Matcher matcher = pattern.matcher(deliveryName);
            if (matcher.matches() == false) {
                throw new InvalidObjectException(
                        "deliveryName '" + deliveryName + "' does not match pattern '" + pattern + "'");
            }
            
            String dateString = matcher.group(1);
            DayOfWeek dayOfWeek = LocalDate.parse(dateString,
                                                  new DateTimeFormatterBuilder().appendPattern("yyyyMMdd")
                                                                                .toFormatter())
                                           .getDayOfWeek();
    
    
            return dayOfWeek;
        }
        
        
        protected LocalDate getDate(Pattern pattern, String deliveryName) throws InvalidObjectException {
            //If the item is not a delivery, skip it
            Matcher matcher = pattern.matcher(deliveryName);
            if (matcher.matches() == false) {
                throw new InvalidObjectException(
                        "deliveryName '" + deliveryName + "' does not match pattern '" + pattern + "'");
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
    }
}
