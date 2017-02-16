package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.ToolResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester.KibanaLoggingStrings;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
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
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main class for starting autonomous component
 * This component is used for validation of metadata from newspaper deliveries.
 * The metadata is ingested into fedora-commons and is now validated against *.xsd and defined rules
 */
public class ValidateXMLMain {
    protected static final Logger log = LoggerFactory.getLogger(ValidateXMLMain.class);

    public static final String AUTONOMOUS_SUCCESSFULL_EVENT = "autonomous.thisSuccessfullEvent";

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
        Tool provideTool(@Named(AUTONOMOUS_SUCCESSFULL_EVENT) String thisSucessfulEventName, QuerySpecification query, DomsRepository domsRepository) {
            Tool f = () -> Stream.of(query)
                    .flatMap(domsRepository::query)
                    .peek(o -> log.trace("Query returned: {}", o))
                    .map(domsItem -> processChildDomsId(thisSucessfulEventName).apply(domsItem))
                    .collect(Collectors.toList())
                    .toString();

            return f;
        }

        /**
         * Validate all xml-contents located as child under the delivery
         *
         * @param thisSucessfulEventName The name of the Event to write is tha validation of all child-items is
         *                               accepted
         * @return
         */
        private Function<DomsItem, String> processChildDomsId(String thisSucessfulEventName) {
            return domsItem -> {
                // Single doms item
                List<ToolResult> toolResults = domsItem.allChildren()
                        .flatMap(childDomsItem -> analyzeXML(childDomsItem))
                        .collect(Collectors.toList());

                // Sort according to result
                final Map<Boolean, List<ToolResult>> toolResultMap = toolResults.stream()
                        .collect(Collectors.groupingBy(toolResult -> toolResult.getResult()));

                List<ToolResult> failingToolResults = toolResultMap.getOrDefault(Boolean.FALSE, Collections.emptyList());
                boolean outcome = false;

                try {
                    String deliveryName = domsItem.getPath();
                    long startDeliveryIngestTime = System.currentTimeMillis();
                    log.info(KibanaLoggingStrings.START_DELIVERY_XML_VALIDATION_AGAINST_XSD, deliveryName);

                    String deliveryEventMessage = failingToolResults.stream()
                            .map(tr -> "---\n" + tr.getHumanlyReadableMessage() + "\n" + tr.getHumanlyReadableStackTrace())
                            .filter(s -> s.trim().length() > 0) // skip blank lines
                            .collect(Collectors.joining("\n"));

                    // outcome was successful only if no toolResults has a FALSE result.
                    outcome = failingToolResults.size() == 0;

                    final String keyword = getClass().getSimpleName();
                    final Date timestamp = new Date();

                    domsItem.appendEvent(keyword, timestamp, deliveryEventMessage, thisSucessfulEventName, outcome);

                    long finishedDeliveryIngestTime = System.currentTimeMillis();
                    log.info(KibanaLoggingStrings.FINISHED_DELIVERY_XML_VALIDATION_AGAINST_XSD, deliveryName, finishedDeliveryIngestTime - startDeliveryIngestTime);

                } catch (Exception e) {
                    failingToolResults.add(ToolResult.fail("id: " + domsItem + " " + "Failed", e));
                }

                return domsItem + " processed. " + failingToolResults.size() + " failed. outcome = " + outcome;
            };
        }

        /**
         * Start validating xml-content in fedora and return results
         *
         * @param domsItem
         * @return
         */
        protected Stream<ToolResult> analyzeXML(DomsItem domsItem) {

            final List<DomsDatastream> datastreams = domsItem.datastreams();

            Optional<DomsDatastream> profileOptional = datastreams.stream()
                    .filter(ds -> ds.getID().equals("XML"))
                    .findAny();

            if (profileOptional.isPresent() == false) {
                return Stream.of();
            }

            Map<String, String> xsdMap = provideXsdRootMap();
            DomsDatastream ds = profileOptional.get();

            try {
                //We are reading this textstring as a String and are aware that thish might leed to encoding problems
                StringReader reader = new StringReader(ds.getDatastreamAsString());
                InputSource inps = new InputSource(reader);

                String rootnameInCurrentXmlFile = getRootTagName(inps);
                String xsdFile = xsdMap.get(rootnameInCurrentXmlFile);
                if (xsdFile == null) {
                    return Stream.of(ToolResult.fail("id: " + domsItem + " " + "Unknown root"));
                }
                URL url = getClass().getClassLoader().getResource(xsdFile);
                reader = new StringReader(ds.getDatastreamAsString());

                if (tryParsing(reader, url)) {
                    //If returning true the parsing is accepted
                    return Stream.of(ToolResult.ok("id: " + domsItem + " " + "XML approved"));
                } else {
                    return Stream.of(ToolResult.fail("id: " + domsItem + " " + "XML invalid"));
                }
            } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException e) {
                log.error(e.getMessage());
                return Stream.of(ToolResult.fail("id: " + domsItem + " " + "Parserexception"));
            }
        }

        /**
         * Try parsing the content inside the reader against the schema located at the url
         * The function returns true if the xml is validated
         *
         * @param reader    Reader containing the content to get parsed
         * @param schemaUrl The url of the schema to validate against
         * @return true if validated
         */
        protected boolean tryParsing(Reader reader, URL schemaUrl) {
            try {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(schemaUrl);
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(reader));
                log.info("Validation of the xml-content is accepted");
                return true;
            } catch (IOException | SAXException e) {
                //This exception is not keept since this exception should just result in registrating that the xml is not validate
                log.info("Validation of the xml-content is rejected");
                return false;
            }
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
        @Named(AUTONOMOUS_SUCCESSFULL_EVENT)
        String thisEventName(ConfigurationMap map) {
            return map.getRequired(AUTONOMOUS_SUCCESSFULL_EVENT);
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }

        @Provides
        @Named("pageSize")
        Integer providePageSize(ConfigurationMap map) {
            return Integer.valueOf(map.getRequired("pageSize"));
        }
    }
}
