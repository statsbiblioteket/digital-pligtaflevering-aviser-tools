package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.kb.stream.StreamTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.EventQuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.JaxbList;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.PdfContentDelegate;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.EventTrigger;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import org.apache.commons.codec.CharEncoding;
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
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;
import static java.util.stream.Collectors.toList;

/**
 * 'PDFContentMain' checks if there is any pdf-files in the delivery which has broken the rule 6.1.11.
 * If this is the case it generates a list of embedded files in the pdf-file.
 * This is dome to make it possible to ignore the error, if there is any
 */
public class PDFContentMain {
    protected static final Logger log = LoggerFactory.getLogger(PDFContentMain.class);

    public static final String VERAPDF_DATASTREAM_NAME = "VERAPDF";
    public static final String DPA_VERAPDF_URL = "dpa.verapdf.url";

    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.DaggerPDFContentMain_PDFContentComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, PDFContentModule.class, BitRepositoryModule.class})
    interface PDFContentComponent {
        Tool getTool();

    }

    interface PDFContentInvoker extends Function<URL, String> {
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
    public static class PDFContentModule {
        public static final String DPA_VERAPDF_REUSEEXISTINGDATASTREAM = "dpa.verapdf.reuseexistingdatastream";

        /**
         * @noinspection PointlessBooleanExpression
         */
        @Provides
        protected Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                                   QuerySpecification workToDoQuery,
                                   DomsRepository domsRepository,
                                   @Named(BITMAG_BASEURL_PROPERTY) String bitrepositoryURLPrefix,
                                   @Named(BitRepositoryModule.BITREPOSITORY_SBPILLAR_MOUNTPOINT) String bitrepositoryMountpoint,
                                   @Named(DPA_VERAPDF_REUSEEXISTINGDATASTREAM) boolean reuseExistingDatastream) {

            final String agent = getClass().getSimpleName();

            // pre-compile
            final XPathExpression leftXPathm;
            final String leftExpressionm = "//ruleId/clause[text() = '6.1.11']";
            try {
                leftXPathm = XPathFactory.newInstance().newXPath().compile(leftExpressionm);
            } catch (XPathExpressionException e) {
                throw new RuntimeException(leftExpressionm, e);
            }


            Tool f = () -> Stream.of(workToDoQuery)
                    .flatMap(domsRepository::query)
                    .peek(o -> log.trace("Query returned: {}", o))
                    .map(StreamTuple::create)
                    .map(st -> st.map((DomsItem roundtripItem) -> {
                                log.info("Start collecting veraPDF-results for the rondtrip: " + roundtripItem.getPath());

                                List<DomsItem> listOfItemsInRoundtrip = roundtripItem.allChildren().collect(toList());
                                List<DomsItem> listOfPdfFiles = listOfItemsInRoundtrip.stream().filter(item -> item.getPath().endsWith(".pdf")).collect(toList());

                                for (DomsItem child : listOfPdfFiles) {

                                    String url = child.datastreams().stream().filter(datastream -> datastream.getMimeType().equals("application/pdf")).findAny().get().getUrl();
                                    String veraresult = child.datastreams().stream().filter(ds -> ds.getId().equals(VERAPDF_DATASTREAM_NAME)).findAny().get().getDatastreamAsString();

                                    long nodes = streamFor(leftXPathm, veraresult).count();

                                    if (nodes > 0) {
                                        URL urlObj = getUrlForBitrepositoryItemPossiblyLocallyAvailable(child, bitrepositoryURLPrefix, bitrepositoryMountpoint, url);

                                        try {
                                            List<String> a = PdfContentDelegate.getListOfEmbeddedFilesFromPdf(urlObj);
                                            JaxbList streamableList = new JaxbList(a);
                                            byte[] pdfContentStream = PdfContentDelegate.processListOfEmbeddedFilesToBytestream().apply(streamableList);
                                            child.modifyDatastreamByValue("contentResult", null, null, pdfContentStream, null, "text/xml", "comment", null);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                return new ArrayList<String>();
                            }
                    ))

                    .collect(toList()); // Results:  X ["foo.pdf: INVALID 1, MANUAL_INTERVENTION 2", "bar.pdf: INVALID 2"]

            return f;
        }


        public URL getUrlForBitrepositoryItemPossiblyLocallyAvailable(DomsItem domsItem, String bitrepositoryURLPrefix, String bitrepositoryMountpoint, String itemURL) {
            if (itemURL.startsWith(bitrepositoryURLPrefix)) {
                final String resourceName;
                resourceName = itemURL.substring(bitrepositoryURLPrefix.length());
                final File file;
                try {
                    Path path = Paths.get(bitrepositoryMountpoint, URLDecoder.decode(resourceName, CharEncoding.UTF_8));
                    file = path.toFile();
                    //This check is only done when the link is to a file in the filesystem, which is how it is used in production
                    if (!file.exists()) {
                        log.error("Unknown link to file " + path.toString());
                        throw new RuntimeException("Unknown link to file " + path.toString());
                    }

                    log.trace("pdf expected to be in:  {}", file.getAbsolutePath());
                    return file.toURI().toURL();
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(domsItem + " '" + resourceName + "' could not get decoded", e);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(domsItem + " '" + resourceName + "' not found", e);
                }
            } else {
                try {
                    return new URL(itemURL);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(domsItem + " url '" + itemURL + " fails", e);
                }
            }
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

        @Provides
        @Named(DPA_VERAPDF_URL)
        String provideVerapdfURL(ConfigurationMap map) {
            return map.getRequired(DPA_VERAPDF_URL);
        }

        @Provides
        @Named(DPA_VERAPDF_REUSEEXISTINGDATASTREAM)
        boolean provideReuseExistingDatastream(ConfigurationMap map) {
            return Boolean.valueOf(map.getDefault(DPA_VERAPDF_REUSEEXISTINGDATASTREAM, "false"));
        }
    }
}
