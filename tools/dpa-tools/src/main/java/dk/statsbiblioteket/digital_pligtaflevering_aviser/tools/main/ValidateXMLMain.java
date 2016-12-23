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
import java.io.StringReader;
import java.net.URL;
import java.util.Collections;
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

    public static void main(String[] args) {

        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerValidateXMLMain_ValidateXMLComponent.builder().configurationMap(m).build().getTool()
        );
    }



    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, IngesterModule.class})
    interface ValidateXMLComponent {
        Tool getTool();
    }

    @Module
    protected static class IngesterModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Tool provideTool(QuerySpecification query, DomsRepository domsRepository) {
            Tool f = () -> Stream.of(query)
                    .flatMap(domsRepository::query)
                    .peek(o -> log.trace("Query returned: {}", o))
                    .map(domsItem -> processChildDomsId(domsRepository).apply(domsItem))
                    .collect(Collectors.toList())
                    .toString();

            return f;
        };


        private Function<DomsItem, String> processChildDomsId(DomsRepository domsRepository) {
            return domsItem -> {
                long startTime = System.currentTimeMillis();

                // Single doms item
                List<ToolResult> toolResults = domsItem.allChildren().stream()
                        .flatMap(childDomsItem -> analyzeXML(childDomsItem, domsRepository))
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

                log.info("{} {} Took: {} ms", keyword, domsItem, (System.currentTimeMillis() - startTime));
                return "domsID " + domsItem + " processed. " + failingToolResults.size() + " failed. outcome = " + outcome;
            };
        }

        /**
         * Start validating xmlfiles in fedora and return results
         * @param domsItem
         * @param domsRepository
         * @return
         */
        protected Stream<ToolResult> analyzeXML(DomsItem domsItem, DomsRepository domsRepository) {

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

                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                StringReader reader = new StringReader(ds.getDatastreamAsString());
                InputSource inps = new InputSource(reader);

                String rootnameInCurrentXmlFile = getRootName(inps);
                String xsdFile = xsdMap.get(rootnameInCurrentXmlFile);
                URL url = getClass().getClassLoader().getResource(xsdFile);
                reader = new StringReader(ds.getDatastreamAsString());
                Schema schema = schemaFactory.newSchema(url);
                Validator validator = schema.newValidator();

                validator.validate(new StreamSource(reader));
                log.trace("IT IS VALID");
            } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException e) {
                e.printStackTrace();
            }


            //If the end of this file is hit without hitting an exception, ok is returned
            return Stream.of(ToolResult.ok("id: " + domsItem + " " + "comment"));
        }



        private String getRootName(InputSource reader) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(reader);
            Element root = xmlDocument.getDocumentElement();
            return root.getTagName();
        }

        /**
         * Previd a map between rootitems in xmlfiles and their corresponding schemafile
         * @return
         */
        @Provides
        Map<String, String> provideXsdRootMap() {
            Map<String, String> xsdMap = new HashMap<String, String>();
            xsdMap.put("article", "Article.xsd");
            xsdMap.put("pdfinfo", "PdfInfo.xsd");
            return xsdMap;
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
