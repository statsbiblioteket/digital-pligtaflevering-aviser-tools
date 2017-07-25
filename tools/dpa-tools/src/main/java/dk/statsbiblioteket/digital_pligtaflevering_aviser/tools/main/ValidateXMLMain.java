package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester.KibanaLoggingStrings;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import javaslang.control.Try;
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
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;

/**
 * Main class for starting autonomous component
 * This component is used for validation of XML data in one or more delivery
 */
public class ValidateXMLMain {
    protected static final Logger log = LoggerFactory.getLogger(ValidateXMLMain.class);

    public static final String AUTONOMOUS_THIS_EVENT = "autonomous.thisEvent";

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

        @Provides
        Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                         QuerySpecification workToDoQuery,
                         DomsRepository domsRepository,
                         DefaultToolMXBean mxBean) {
            final String agent = ValidateXMLModule.class.getSimpleName();

            Tool f = () -> Stream.of(workToDoQuery)
                    .flatMap(domsRepository::query)
                    .peek(domsItem -> log.trace("Processing: {}", domsItem))
                    .map(domsItem -> processChildDomsId(mxBean).apply(domsItem))
                    .peek(tr -> tr.getItem().appendEvent(agent, new Date(), tr.getHumanlyReadableMessage(), eventName, tr.getResult()))
                    .count() + " items processed";

            return f;
        }

        /**
         * Validate all xml-contents located as child under the delivery
         *
         * @return
         * @param mxBean
         */
        private Function<DomsItem, ToolResult> processChildDomsId(DefaultToolMXBean mxBean) {
            return domsItem -> {
                String deliveryName = domsItem.getPath();
                long startDeliveryIngestTime = System.currentTimeMillis();
                log.info(KibanaLoggingStrings.START_DELIVERY_XML_VALIDATION_AGAINST_XSD, deliveryName);

                // Single doms item
                Map<Boolean, List<Try<ToolResult>>> toolResultMap = domsItem.allChildren()
                        .flatMap(childDomsItem -> analyzeXML(childDomsItem, mxBean))
                        .collect(Collectors.partitioningBy(Try::isSuccess));

                final List<Try<ToolResult>> failed = toolResultMap.getOrDefault(FALSE, emptyList());
                failed.forEach(
                        t -> log.error("failed", t.getCause())
                );

                final Map<Boolean, List<ToolResult>> notFailedMap = toolResultMap.getOrDefault(TRUE, emptyList()).stream()
                        .map(Try::get)
                        .collect(Collectors.partitioningBy(toolResult -> toolResult.getResult()));

                final List<ToolResult> successful = notFailedMap.getOrDefault(TRUE, emptyList());
                final List<ToolResult> notSuccessful = notFailedMap.getOrDefault(FALSE, emptyList());

                // we now have organized the responses in "failed", "succesful", and "notSucessful"

                final boolean outcome = failed.size() == 0 && notSuccessful.size() == 0;

                StringBuilder message = new StringBuilder();
                if (outcome == false) {
                    message.append("item: " + domsItem + "\n");
                    if (failed.size() > 0) {
                        message.append("failed (see full traces in log file)\n");
                        message.append("==============\n");
                        failed.forEach(tr -> message.append(tr.getCause().getMessage()).append("\n"));
                        message.append("\n");
                    }
                    if (notSuccessful.size() > 0) {
                        message.append("not successful\n");
                        message.append("==============\n");
                        notSuccessful.forEach(tr -> message.append("item: ").append(tr.getItem()).append(", reason: ").append(tr.getHumanlyReadableMessage()).append("\n"));
                        message.append("\n");
                    }

                    if (successful.size() > 0) {
                        message.append("successful\n");
                        message.append("==========\n");
                        successful.forEach(tr -> message.append("item: ").append(tr.getItem()).append(", reason: ").append(tr.getHumanlyReadableMessage()).append("\n"));
                        message.append("\n");
                    }
                } else {
                    message.append(successful.size() + " valid.");
                }

                final String deliveryEventMessage = message.toString();

                long finishedDeliveryIngestTime = System.currentTimeMillis();
                log.info(KibanaLoggingStrings.FINISHED_DELIVERY_XML_VALIDATION_AGAINST_XSD, deliveryName, finishedDeliveryIngestTime - startDeliveryIngestTime);

                if (outcome == true) {
                    return ToolResult.ok(domsItem, deliveryEventMessage);
                } else {
                    return ToolResult.fail(domsItem, deliveryEventMessage);
                }
            };
        }

        /**
         * Start validating xml-content in fedora and return results
         *
         * @param domsItem
         * @param mxBean
         * @return
         */
        protected Stream<Try<ToolResult>> analyzeXML(DomsItem domsItem, DefaultToolMXBean mxBean) {

            final List<DomsDatastream> datastreams = domsItem.datastreams();

            Optional<DomsDatastream> profileOptional = datastreams.stream()
                    .filter(ds -> ds.getId().equals("XML"))
                    .findAny();

            if (profileOptional.isPresent() == false) {
                return Stream.of();
            }

            mxBean.idsProcessed++;
            mxBean.currentId = domsItem.getDomsId().toString();

            Map<String, String> xsdMap = provideXsdRootMap();
            DomsDatastream ds = profileOptional.get();

            // Note:  We ignore character set problems for now.
            final String datastreamAsString = ds.getDatastreamAsString();

            return Stream.of(Try.of(() -> {

                String rootnameInCurrentXmlFile = getRootTagName(new InputSource(new StringReader(datastreamAsString)));
                String xsdFile = xsdMap.get(rootnameInCurrentXmlFile);
                if (xsdFile == null) {
                    return ToolResult.fail(domsItem, "id: " + domsItem + " " + "Unknown root:" + rootnameInCurrentXmlFile);
                }
                URL url = getClass().getClassLoader().getResource(xsdFile);

                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(url);
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(new StringReader(datastreamAsString))); // only succedes if valid
                log.trace("{}: XML valid", domsItem.getDomsId());

                return ToolResult.ok(domsItem, "id: " + domsItem.getDomsId() + " XML valid");
            }));
        }

        /**
         * Get the rootname of the xml-stream delivered to the function
         *
         * @param reader
         * @return
         * @throws ParserConfigurationException
         * @throws IOException
         * @throws SAXException
         * @throws XPathExpressionException
         */
        protected String getRootTagName(InputSource reader) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(reader);
            Element root = xmlDocument.getDocumentElement();
            return root.getTagName();
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

        /**
         * Provide the parameter to be written as sucessfull when the component has finished
         *
         * @param map
         * @return
         */
        @Provides
        @Named(AUTONOMOUS_THIS_EVENT)
        String thisEventName(ConfigurationMap map) {
            return map.getRequired(AUTONOMOUS_THIS_EVENT);
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }

    }
}

