package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.kb.stream.StreamTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.EventQuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.JaxbList;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.PdfContentUtils;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import dk.statsbiblioteket.util.xml.DOM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.inject.Named;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
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

    public static final String PDF_CONTENT_NAME = "PDFCONTENT";

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

    public static boolean streamForHasContent(XPathExpression xpath, String xml) {
        try {
            Document xmlDocument = DOM.stringToDOM(xml);
            NodeList nodeList = (NodeList) xpath.evaluate(xmlDocument, XPathConstants.NODESET);
            return nodeList.getLength()>0;
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Failed to process " + xml, e);
        }
    }


    /**
     * @noinspection WeakerAccess
     */
    @Module
    public static class PDFContentModule {

        /**
         * @noinspection PointlessBooleanExpression
         */
        @Provides
        protected Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                                   QuerySpecification workToDoQuery,
                                   DomsRepository domsRepository,
                                   @Named(BITMAG_BASEURL_PROPERTY) String bitrepositoryURLPrefix,
                                   @Named(BitRepositoryModule.BITREPOSITORY_SBPILLAR_MOUNTPOINT) String bitrepositoryMountpoint) {

            final String agent = getClass().getSimpleName();

            // pre-compile
            final XPathExpression leftXPathm;
            final String leftExpressionm = "//testAssertions/ruleId/clause[text() = '6.1.11']";
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
                                    String veraresult = child.datastreams().stream().filter(ds -> ds.getId().equals(VeraPDFInvokeMain.VERAPDF_DATASTREAM_NAME)).findAny().get().getDatastreamAsString();

                                    if (streamForHasContent(leftXPathm, veraresult)) {
                                        URL urlObj = VeraPDFInvokeMain.VeraPDFInvokeModule.getUrlForBitrepositoryItemPossiblyLocallyAvailable(child, bitrepositoryURLPrefix, bitrepositoryMountpoint, url);

                                        try {
                                            List<String> a = PdfContentUtils.getListOfEmbeddedFilesFromPdf(urlObj);
                                            JaxbList streamableList = new JaxbList(a);
                                            byte[] pdfContentStream = PdfContentUtils.processListOfEmbeddedFilesToBytestream().apply(streamableList);
                                            child.modifyDatastreamByValue(PDF_CONTENT_NAME, null, null, pdfContentStream, null, "text/xml", "URL: " + url, null);
                                        } catch (IOException e) {
                                            log.error("ContentExtractionError", e);
                                            roundtripItem.appendEvent(new DomsEvent(agent, new Date(), "failed" + urlObj.getPath(), eventName, true));
                                            return false;
                                        }
                                    }
                                }
                                roundtripItem.appendEvent(new DomsEvent(agent, new Date(), "processing done", eventName, true));
                                return true;
                            }
                    ))

                    .collect(toList()); // Results:  X ["foo.pdf: INVALID 1, MANUAL_INTERVENTION 2", "bar.pdf: INVALID 2"]

            return f;
        }

        @Provides
        protected Function<EventQuerySpecification, Stream<DomsId>> sboiEventIndexSearch(SBOIEventIndex<Item> index) {
            return query -> VeraPDFInvokeMain.VeraPDFInvokeModule.sboiEventIndexSearch(query, index).stream();
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }

    }
}
