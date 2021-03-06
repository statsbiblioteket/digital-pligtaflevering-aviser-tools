package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.kb.stream.StreamTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.PdfContentUtils;
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
import javax.xml.bind.JAXBException;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf.VeraPDFOutputValidation.REMOVE_NEWLINES_REGEX;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf.VeraPDFOutputValidation.severenessFor;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * 'VeraPDFAnalyzeMain' analyses the result from veraPdf, it generates a report about the significancy of each rule,
 * that does not correspond to the metadata written by 'VeraPDFInvokeModule'
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
     * Parse the XML-content into a list of files
     * @param xml
     * @return If xml is null return an empty list
     */
    public static List<String> getListOfEmbeddedFilesFromXml(DomsDatastream xml) throws JAXBException {
        if(xml==null) {
            return new ArrayList<>();
        }
        return PdfContentUtils.getListOfEmbeddedFilesFromXml(xml.getDatastreamAsString()).getList();
    }


    /**
     * @noinspection WeakerAccess
     */
    @Module
    public static class VeraPDFAnalyzeModule {

        public static final String VERAPDFREPORT_DATASTREAM = "VERAPDFREPORT";

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
                    .map(st -> st.map((DomsItem roundtripItem) -> handleVerapdfResults(eventName, mxBean, agent, failedXPath, leftXPath, rightXPath, roundtripItem)
                    ))
                    .peek((StreamTuple<DomsItem, List<String>> o) -> {
                    })
                    .collect(toList()); // Results:  X ["foo.pdf: INVALID 1, MANUAL_INTERVENTION 2", "bar.pdf: INVALID 2"]

            return f;
        }

        /**
         * Get a report of failed paragraphs in a delivery.
         * The report is split up into one file at a time
         * @param eventName
         * @param mxBean
         * @param agent
         * @param failedXPath
         * @param leftXPath
         * @param rightXPath
         * @param roundtripItem
         * @return '// Results:  X ["foo.pdf: INVALID 1, MANUAL_INTERVENTION 2", "bar.pdf: INVALID 2"]'
         */
        private List<String> handleVerapdfResults(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                                                  DefaultToolMXBean mxBean,
                                                  String agent,
                                                  XPathExpression failedXPath,
                                                  XPathExpression leftXPath,
                                                  XPathExpression rightXPath,
                                                  DomsItem roundtripItem) {
            log.info("Start collecting veraPDF-results for the roundtrip: " + roundtripItem.getPath());
            List<StreamTuple<String, Map<SeverenessLevel, List<StreamTuple<String, String>>>>> pathSeverenessProblemsList = roundtripItem.allChildren()
                    .peek(i -> mxBean.currentId = String.valueOf(i))
                    .peek(i -> mxBean.idsProcessed++)
                    .map(item -> new StreamTuple<>(item.getPath(), item))
                    .filter(item -> item.left().endsWith(".pdf"))
                    .map(stx -> stx.map((DomsItem child) -> getSeverenessLevel(failedXPath, leftXPath, rightXPath, child)))
                    .peek((StreamTuple<String, Map<SeverenessLevel, List<StreamTuple<String, String>>>> o) -> {})
                    .sorted(Comparator.comparing(e -> e.left())) // path name
                    .collect(toList());

            long[] badLines = new long[1];

            // Create report for roundtripItem of all bad items.
            String roundtripItemReport = pathSeverenessProblemsList.stream()
                    .peek((StreamTuple<String, Map<SeverenessLevel, List<StreamTuple<String, String>>>> o) -> {
                    })
                    .map(stx -> stx.left() + "\n==============\n" + stx.right().entrySet().stream()
                            .sorted(Comparator.comparing(Map.Entry::getKey))
                            .map(entry -> entry.getKey() + "\n"
                                    + "------------------------------------------\n"
                                    + entry.getValue().stream()
                                    .map(st0 -> st0.left() + ": " + st0.right())
                                    .sorted()
                                    .peek(s -> {
                                        badLines[0]++;
                                    })
                                    .collect(joining("\n")))
                            .collect(joining("\n\n")))
                    .collect(joining("\n\n"));


            String actualRoundtripItemReport = badLines[0] > 0 ? roundtripItemReport : "Nothing non-acceptable reported by VeraPDF";

            roundtripItem.modifyDatastreamByValue(VERAPDFREPORT_DATASTREAM, null, null, actualRoundtripItemReport.getBytes(StandardCharsets.UTF_8), null, "text/plain", null, new Date().getTime());


            // "foo.pdf: INVALID 1, MANUAL_INTERVENTION 2"
            List<String> toolReportLine = pathSeverenessProblemsList.stream()
                    .map(stx -> stx.left() + ": " + stx.right().entrySet().stream()
                            .sorted(Comparator.comparing(Map.Entry::getKey))
                            .map(e -> e.getKey() + " " + e.getValue().size())
                            .collect(joining(", "))
                    ).collect(toList());

            roundtripItem.appendEvent(new DomsEvent(agent, new Date(), "Processed: \n" + toolReportLine, eventName, badLines[0] == 0));

            return toolReportLine;
        }

        /**
         * Get a list af failed pdf-paragraphs in the DomsItem, the result is returned sorted into severenesslevel
         * Only failed paragraphs that is considered 'bad' in SeverenessLevel is returned
         * @param failedXPath
         * @param leftXPath
         * @param rightXPath
         * @param child
         * @return
         */
        private Map<SeverenessLevel, List<StreamTuple<String, String>>> getSeverenessLevel(XPathExpression failedXPath,
                                                                                           XPathExpression leftXPath,
                                                                                           XPathExpression rightXPath,
                                                                                           DomsItem child) {
            Map<SeverenessLevel, List<StreamTuple<String, String>>> groupedBySevereness = null;
            //Get the stream of pdf-failures accoring to pdfa standard
            String verapdf_xml = child.datastream(VeraPDFInvokeMain.VERAPDF_DATASTREAM_NAME).getDatastreamAsString();

            //Get the stream of xml containing list of embedded files
            DomsDatastream embeddedFiles_xml = child.datastream(PDFContentMain.PDF_CONTENT_NAME);

            List<String> listOfEmbeddedFilesFromXml;
            try {
                listOfEmbeddedFilesFromXml = getListOfEmbeddedFilesFromXml(embeddedFiles_xml);


            log.info("Collecting result from the delivery: " + child.getPath());
            groupedBySevereness = streamFor(failedXPath, verapdf_xml)
                    .map(node -> Try.of(() -> new StreamTuple<>(
                            leftXPath.evaluate(node).replaceAll(REMOVE_NEWLINES_REGEX, " "),
                            rightXPath.evaluate(node).replaceAll(REMOVE_NEWLINES_REGEX, " ")
                    )).get())
                    .peek((StreamTuple<String, String> s) -> {
                    })
                    // map: severenesslevel -> list<problems>
                    .collect(groupingBy(st3 -> severenessFor(st3.left(), listOfEmbeddedFilesFromXml), mapping(st3 -> st3, toList())));

            String fullChildReport = groupedBySevereness.entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(entry -> entry.getKey() + "\n"
                            + "------------------------------------------\n"
                            + entry.getValue().stream()
                            .map(st0 -> st0.left() + ": " + st0.right())
                            .sorted()
                            .collect(joining("\n")))
                    .collect(joining("\n\n"));

            if (fullChildReport.length() == 0) {
                fullChildReport = "VeraPDF did not report any problems.";
            }
            child.modifyDatastreamByValue(VERAPDFREPORT_DATASTREAM, null, null, fullChildReport.getBytes(StandardCharsets.UTF_8), null, "text/plain", null, new Date().getTime());

            // Only keep the bad ones
            groupedBySevereness.entrySet().removeIf(e -> e.getKey().isBad() == false);

            } catch (JAXBException e) {
                log.error("Error parsing embedded files", e);
                StreamTuple ss = new StreamTuple("Unparsable content", "Embedded files could not get parsed");
                ArrayList arl = new ArrayList<StreamTuple<String, String>>();
                arl.add(ss);

                groupedBySevereness = new HashMap<SeverenessLevel, List<StreamTuple<String, String>>>();
                groupedBySevereness.put(SeverenessLevel.UNKNOWN, arl);
            }

            return groupedBySevereness;
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }

    }
}
