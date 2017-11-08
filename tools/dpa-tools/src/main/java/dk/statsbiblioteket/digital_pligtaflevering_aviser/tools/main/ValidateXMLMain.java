package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReport;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.streams.IdValue;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.DomsValue;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester.KibanaLoggingStrings;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import javaslang.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.inject.Named;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event.STOPPED_STATE;

/**
 * Main class for starting autonomous component This component is used for validation of XML data in one or more
 * delivery
 */
public class ValidateXMLMain {
    protected static final Logger log = LoggerFactory.getLogger(ValidateXMLMain.class);

    public static void main(String[] args) {

        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerValidateXMLMain_ValidateXMLComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, ValidateXMLModule.class})
    interface ValidateXMLComponent {
        Tool getTool();
    }

    /**
     * @noinspection Convert2MethodRef
     */
    @Module
    protected static class ValidateXMLModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        /**
         * @noinspection PointlessBooleanExpression
         */
        @Provides
        Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                         QuerySpecification workToDoQuery,
                         DomsRepository domsRepository,
                         DefaultToolMXBean mxBean) {
            final String agent = ValidateXMLMain.class.getSimpleName();

            Tool f = () -> Stream.of(workToDoQuery)
                    .flatMap(domsRepository::query)
                    .peek(domsItem -> log.trace("Processing: {}", domsItem))
                    .map(DomsValue::create)
                    .map(c -> c.map(domsItem -> processChildDomsId(mxBean, eventName).apply(domsItem)))
                    .peek(c -> {
                        c.id().appendEvent(new DomsEvent(agent, new Date(), c.value().getHumanlyReadableMessage(), eventName, c.value().isSuccess()));
                        if (c.value().isSuccess() == false) {
                            c.id().appendEvent(new DomsEvent(agent, new Date(), "autonomous component failed", STOPPED_STATE, false));
                        }
                    })
                    .count() + " items processed";

            return f;
        }

        /**
         * Validate all xml-contents located as child under the delivery
         *
         * @param mxBean
         * @param eventName
         * @return
         */
        private Function<DomsItem, ToolResult> processChildDomsId(DefaultToolMXBean mxBean, String eventName) {
            return parentDomsItem -> {
                String deliveryName = parentDomsItem.getPath();
                long startDeliveryIngestTime = System.currentTimeMillis();
                log.info(KibanaLoggingStrings.START_DELIVERY_XML_VALIDATION_AGAINST_XSD, deliveryName);

                // Single doms item

                final String agent = ValidateXMLMain.class.getSimpleName();

                //final Function<IdValue<DomsItem, U>, Stream<V>> idValueStreamFunction = c -> c.flatMap(v -> analyzeXML(v, mxBean));
                List<IdValue<DomsItem, Either<Exception, ToolResult>>> toolResults = parentDomsItem.allChildren()
                        .map(DomsValue::create)
                        .flatMap(c -> c.flatMap(domsItem -> domsItem.datastreams().stream()
                                .filter(ds -> ds.getId().equals("XML"))
                                .peek(z -> mxBean.idsProcessed++)
                                .peek(z -> mxBean.currentId = c.toString())
                                .map(ds -> analyzeXML(ds, mxBean, eventName))
                                // Save individual result as event on node.
                                .peek(either -> either.bimap(
                                        e -> domsItem.appendEvent(new DomsEvent(agent, new Date(), stacktraceFor(e), eventName, false)),
                                        tr -> domsItem.appendEvent(new DomsEvent(agent, new Date(), tr.getHumanlyReadableMessage(), eventName, tr.isSuccess())))
                                ))
                        )
                        .collect(Collectors.toList());

                ToolResultsReport trr = new ToolResultsReport(ToolResultsReport.OK_COUNT_FAIL_LIST_RENDERER, (id, t) -> log.error("id: {}", id, t));

                ToolResult result = trr.apply(parentDomsItem, toolResults);

                long finishedDeliveryIngestTime = System.currentTimeMillis();
                log.info(KibanaLoggingStrings.FINISHED_DELIVERY_XML_VALIDATION_AGAINST_XSD, deliveryName, finishedDeliveryIngestTime - startDeliveryIngestTime);

                return result;
            };
        }

        /**
         * Start validating xml-content in fedora and return results
         *
         * @param ds        DomsDatastream containing the XML file to validate.
         * @param mxBean
         * @param eventName
         * @return
         */
        protected Either<Exception, ToolResult> analyzeXML(DomsDatastream ds, DefaultToolMXBean mxBean, String eventName) {

            Map<String, String> xsdMap = provideXsdRootMap();
            // Note:  We ignore character set problems for now.
            final String datastreamAsString = ds.getDatastreamAsString();

            try {
                final ToolResult toolResult;

                String rootnameInCurrentXmlFile = getRootTagName(new InputSource(new StringReader(datastreamAsString)));
                String xsdFile = xsdMap.get(rootnameInCurrentXmlFile);
                if (xsdFile == null) {
                    toolResult = ToolResult.fail("Unknown root:" + rootnameInCurrentXmlFile);
                } else {
                    URL url = Objects.requireNonNull(getClass().getClassLoader().getResource(xsdFile), "xsdFile not found: " + xsdFile);

                    Validator validator = getValidatorFor(url);

                    final StreamSource source = new StreamSource(new StringReader(datastreamAsString));
                    validator.validate(source); // throws exception if invalid.
                    log.trace("{}: XML valid", ds.getDomsItem().getDomsId().id());
                    toolResult = ToolResult.ok(""); // Empty message if ok.  Makes GUI presentation smaller.
                }
                return Either.right(toolResult);
            } catch (Exception e) {
                return Either.left(e);
            }
        }

        /**
         * Get the rootname of the xml-stream delivered to the function
         *
         * @param reader
         * @return
         *
         * @throws ParserConfigurationException
         * @throws IOException
         * @throws SAXException
         * @throws XPathExpressionException
         */
        protected String getRootTagName(InputSource reader) {
            try {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                Document xmlDocument = builder.parse(reader);
                Element root = xmlDocument.getDocumentElement();
                return root.getTagName();
            } catch (ParserConfigurationException | IOException | SAXException e) {
                throw new RuntimeException("getRootTagName", e);
            }
        }

        /**
         * Previd a map between rootitems in xmlfiles and their corresponding schemafile
         *
         * @return
         */
        protected Map<String, String> provideXsdRootMap() {
            Map<String, String> xsdMap = new HashMap<String, String>();
            xsdMap.put("article", "xmlValidation/Article.xsd");
            xsdMap.put("pdfinfo", "xmlValidation/PdfInfo.xsd");
            return xsdMap;
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }

    }

    public static Validator getValidatorFor(URL url) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            return schemaFactory.newSchema(url).newValidator();
        } catch (SAXException e) {
            throw new RuntimeException("getValidatorFor: url=" + url, e);
        }
    }

    public static String stacktraceFor(Throwable t) {
        // https://stackoverflow.com/a/1149721/53897
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
}

