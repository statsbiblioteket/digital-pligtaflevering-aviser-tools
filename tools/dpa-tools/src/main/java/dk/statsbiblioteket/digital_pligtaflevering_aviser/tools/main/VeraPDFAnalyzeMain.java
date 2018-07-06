package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.kb.stream.StreamTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf.SeverenessLevel;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf.VeraPDFOutputValidation.REMOVE_NEWLINES_REGEX;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf.VeraPDFOutputValidation.severenessFor;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Unfinished
 */
public class VeraPDFAnalyzeMain {
    protected static final Logger log = LoggerFactory.getLogger(VeraPDFAnalyzeMain.class);

    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerVeraPDFAnalyzeMain_VeraPdfAnalyzeDaggerComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, VeraPDFAnalyzeModule.class, BitRepositoryModule.class})
    interface VeraPdfAnalyzeDaggerComponent {
        Tool getTool();
    }

    public static Stream<Node> streamFor(XPathExpression xpath, String xml) {
        NodeList nodeList = null;
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            nodeList = (NodeList) xpath.evaluate(xmlDocument, XPathConstants.NODESET);
        } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException("Failed to process " + xml, e);
        }
        // https://stackoverflow.com/a/23361853/53897
        return IntStream.range(0, nodeList.getLength())
                .mapToObj(nodeList::item);
    }

    /**
     * @noinspection WeakerAccess
     */
    @Module
    public static class VeraPDFAnalyzeModule {
        public static final String VERAPDF_INVOKED = "VeraPDF_Invoked";
        public static final String DPA_VERAPDF_FLAVOR = "dpa.verapdf.flavor";
        public static final String DPA_VERAPDF_REUSEEXISTINGDATASTREAM = "dpa.verapdf.reuseexistingdatastream";

        public static final String VERAPDF_REPORT_DATASTREAM = "VERAPDF_REPORT";

        /**
         * @noinspection PointlessBooleanExpression
         */
        @Provides
//        Runnable provideRunnable(Modified_SBOIEventIndex index, DomsEventStorage<Item> domsEventStorage, Stream<EventTrigger.Query> queryStream, Task task) {
        protected Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                                   QuerySpecification workToDoQuery,
                                   DomsRepository domsRepository,
                                   EnhancedFedora efedora,
                                   DomsEventStorage<Item> domsEventStorage,
                                   DefaultToolMXBean mxBean) {

            final String agent = getClass().getSimpleName();

            // pre-compile

            final String expression = "//status[text() = 'FAILED']/..";
            final XPathExpression failedXPath;
            try {
                failedXPath = XPathFactory.newInstance().newXPath().compile(expression);
            } catch (XPathExpressionException e) {
                throw new RuntimeException(expression, e);
            }

            final XPathExpression leftXPath;
            final String leftExpression = "ruleId/clause/text()";
            try {
                leftXPath = XPathFactory.newInstance().newXPath().compile(leftExpression);
            } catch (XPathExpressionException e) {
                throw new RuntimeException(leftExpression, e);
            }

            final XPathExpression rightXPath;
            final String rightExpression = "concat(ruleId/clause/text(), ': ', message/text(), ' [ ', location/context/text(), ' ]')";
            try {
                rightXPath = XPathFactory.newInstance().newXPath().compile(rightExpression);
            } catch (XPathExpressionException e) {
                throw new RuntimeException(rightExpression, e);
            }


            /*
                                         <testAssertions>
                                            <ordinal>3446</ordinal>
                                            <ruleId>
                                                <specification>ISO_19005_1</specification>
                                                <clause>6.2.3</clause>
                                                <testNumber>4</testNumber>
                                            </ruleId>
                                            <status>FAILED</status>
                                            <message>If an uncalibrated colour space is used in a file then that file shall contain a PDF/A-1
                                        OutputIntent, as defined in 6.2.2
                                                </message>
                                            <location>
                                                <level>CosDocument</level>
                                                <context>root/document[0]/pages[0](4 0 obj PDPage)/contentStream[0]/operators[4138]/fillCS[0]</context>
                                            </location>
                                        </testAssertions>
            */

            Tool f = () -> Stream.of(workToDoQuery)
                    .flatMap(domsRepository::query)
                    .peek(o -> log.trace("Query returned: {}", o))
                    .map(StreamTuple::create)
                    .map(st -> st.map((DomsItem roundtripItem) -> roundtripItem.allChildren()
                            .peek(i -> mxBean.currentId = String.valueOf(i))
                            .peek(i -> mxBean.idsProcessed++)
                            .map(i -> new StreamTuple<>(i.getPath(), i))
                            .flatMap(st2 -> st2.flatMap((DomsItem child) -> child.datastreams().stream()
                                    // only look at items with a verapdf datastream
                                    .filter(ds -> ds.getId().equals(VeraPDFInvokeMain.VERAPDF_DATASTREAM_NAME))
                                    .map(DomsDatastream::getDatastreamAsString) // VeraPDF XML output
                                    .map((String xml) -> streamFor(failedXPath, xml)
                                            .map(node -> Try.of(() -> new StreamTuple<>(
                                                    leftXPath.evaluate(node).replaceAll(REMOVE_NEWLINES_REGEX, " "),
                                                    rightXPath.evaluate(node).replaceAll(REMOVE_NEWLINES_REGEX, " ")
                                            )).get())
                                            .peek((StreamTuple<String, String> s) -> {
                                            })
                                            .collect(groupingBy(st3 -> severenessFor(st3.left()), mapping(st3 -> st3, toList()))))

                                    // create full report on item with everything grouped by severeness level
                                    .peek((Map<SeverenessLevel, List<StreamTuple<String, String>>> m) -> {
                                        String report = m.entrySet().stream()
                                                .sorted(Comparator.comparing(Map.Entry::getKey))
                                                .map(entry -> entry.getKey() + "\n"
                                                        + "------------------------------------------\n"
                                                        + entry.getValue().stream()
                                                        // uniq -c
                                                        .collect(groupingBy(Function.identity(), counting())).entrySet().stream()
                                                        .sorted(Comparator.comparing(Map.Entry::getKey))
                                                        .map(entry2 -> entry2.getValue() + ": " + entry2.getKey().right())
                                                        .collect(joining("\n")))
                                                .collect(joining("\n\n"));
                                        child.modifyDatastreamByValue(VERAPDF_REPORT_DATASTREAM, null, null, report.getBytes(StandardCharsets.UTF_8), null, "text/plain", null, new java.util.Date().getTime());
                                    })
                                    // only collect those with a bad severitylevel for the roundtrip report
                                    .map(m -> m.entrySet().stream()
                                            .filter(entry -> entry.getKey().isBad())
                                            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
                                    )
                                    .filter(m -> m.size() > 0)

                            ))
                            .collect(toList())
                            .peek(m ->
                            {

                            })
                            //                           .map(m -> m.map( n -> n))
                            //.collect(groupingBy(m -> m.left(), m -> m.right()))
                            // create and set roundtrip report

                            .peek(System.out::println) // set global report
                            // Create simple summary for log file.
                            .map((StreamTuple<String, Map<SeverenessLevel, List<StreamTuple<String, String>>>> m) ->
                                    m.left() + ": " + m.right().entrySet().stream()
                                            .sorted()
                                            .map(e -> e.getKey() + " " + e.getValue().size())
                                            .collect(joining(", ")) // foo.pdf: INVALID 1, MANUAL_INTERVENTION 2
                            )
                            .collect(toList()))
                    )
                    .collect(toList());  // Results:  X ["foo.pdf: INVALID 1, MANUAL_INTERVENTION 2", "bar.pdf: INVALID 2"]

/*
        List<Boolean> successful=eithersFromRoundtrip.get(Boolean.TRUE).stream()
        .map(either->either.right().get())
        .collect(toList());
        List<String> failed=eithersFromRoundtrip.get(Boolean.FALSE).stream()
        .map(either->DomsItemTuple.stacktraceFor(either.left().get()))
        .collect(toList());

        if(failed.size()==0)

        {
        roundtripItem.appendEvent(new DomsEvent(agent,new Date(),successful.size()+" processed",eventName,true));
        return true;
        }else

        {
        // we have encountered exceptions not handled lower down.  Report those.
        roundtripItem.appendEvent(new DomsEvent(agent,new Date(),String.join("\n\n",failed),eventName,false));
        return false;
        }
        }))
        .

        collect(toList());
*/
            return f;
        }

/*
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
         */

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }

    }
}
