package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import com.google.common.base.Throwables;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.kb.stream.StreamTuple;
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
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.DomsItemTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester.KibanaLoggingStrings;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import javaslang.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static java.util.stream.Collectors.toList;

/**
 * <p>
 * Main class for starting autonomous component This component is used for regenerating the "checksums.txt" file
 * containing MD5 sums in the delivery.  They should be identical (except for line endings and sorting) to the ones
 * supplied by InfoMedia.</p>
 * <p>Three kinds of files are created by the ingester for each paper in the daily delivery:
 * <ol>
 * <li>#1 An XML file for each PDF file containg metadata and a rough ASCII outlining</li>
 * <li>#2 A PDF pr physical page in the newspaper organized under "pages"</li>
 * <li>#3 Article XML files - zero or more for each paper organized under "articles" </li>
 * </ol>
 * </p>
 * <p>Important:  Until the underlying efedora is ensured threadsafe do <it>not</it> attempt to parallize the
 * stream.</p>
 *
 * @noinspection WeakerAccess
 */
public class RegenerateChecksumfileMain {
    protected static final Logger log = LoggerFactory.getLogger(RegenerateChecksumfileMain.class);

    public static void main(String[] args) {

        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerRegenerateChecksumfileMain_RegenerateChecksumfileComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, RegenerateChecksumfileModule.class})
    interface RegenerateChecksumfileComponent {
        Tool getTool();
    }

    /**
     * @noinspection WeakerAccess, Convert2MethodRef
     */
    @Module
    protected static class RegenerateChecksumfileModule {
        Logger log = LoggerFactory.getLogger(RegenerateChecksumfileMain.class);  // short name

        /**
         * @noinspection PointlessBooleanExpression, UnnecessaryLocalVariable, unchecked
         */
        @Provides
        Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                         QuerySpecification workToDoQuery,
                         DomsRepository domsRepository,
                         DefaultToolMXBean mxBean) {
            final String agent = RegenerateChecksumfileMain.class.getSimpleName();

            //
            Tool f = () -> Stream.of(workToDoQuery)
                    .flatMap(domsRepository::query)
                    .peek(domsItem -> log.trace("Processing: {}", domsItem))
                    // roundtrip node for a day delivery and we need to process all the children to get the combined md5sum.
                    .map(DomsItemTuple::create)
                    .map(st0 -> st0.map(roundtripItem -> roundtripItem.children()
                                    .flatMap((DomsItem paperItem) -> Stream.concat(
                                            // each paper has "pages" and "articles" subnodes.  concat the processing of both, until a nice way of splitting a stream is found.
                                            paperItem.children()
                                                    .filter(paperPartItem -> paperPartItem.getPath().endsWith("pages"))
                                                    .flatMap((DomsItem pagesItem) -> pagesItem.children()
                                                            // each page has XML datastream and a subnode with an external link to bit repo to the PDF file
                                                            .flatMap((DomsItem pageItem) -> Stream.concat(
                                                                    // #1
                                                                    Stream.of(pageItem)
                                                                            .map(DomsItemTuple::create)
                                                                            .map(st2 -> st2.map(xmlItem -> xmlItem.getDataStreamInputStream("XML")))
                                                                            .map(st2 -> st2.map((xmlItem, is) -> md5ForClosableInputStream(is) + "  " + checksumFilePathFor(xmlItem) + ".xml"))
                                                                    ,
                                                                    // #2
                                                                    pageItem.children()
                                                                            .map(DomsItemTuple::create)
                                                                            .map(st2 -> st2.map(pdfItem -> pdfItem.getDataStreamInputStream("CONTENTS")))
                                                                            .map(st2 -> st2.map((pdfItem, is) -> md5ForClosableInputStream(is) + "  " + checksumFilePathFor(pdfItem)))
                                                            )))
                                            , paperItem.children()
                                                    .filter(paperPartItem -> paperPartItem.getPath().endsWith("articles"))
                                                    .flatMap((DomsItem articlesItem) -> articlesItem.children()
                                                            .map(DomsItemTuple::create)
                                                            .map(st -> st.map(articleItem -> articleItem.getDataStreamInputStream("XML")))
                                                            // #3
                                                            .map(st -> st.map((articleItem, is) -> md5ForClosableInputStream(is) + "  " + checksumFilePathFor(articleItem) + ".xml"))
                                                    )
                                            )
                                    )
                                    .peek(st -> st.peek((checksummedItem, md5sumLine) -> log.trace("{}->{}", checksummedItem, md5sumLine)))
                                    .map(st -> st.right())

                                    .peek(s -> mxBean.currentId = s)
                                    .peek(s -> mxBean.idsProcessed++)
                                    .collect(Collectors.joining("\n")) + "\n" // collect all generated md5sum lines into a checksums.txt compatible "file"
                            )
                    )
                    // log the result (and document what the stream currently contains)
                    .peek((StreamTuple<DomsItem, String> st) -> log.info("{}", st))

                    // store md5sums "file" in MD5SUMS datastream on roundtrip item.
                    .peek(st -> st.peek((roundtripItem, md5sum) -> roundtripItem.modifyDatastreamByValue(
                            "MD5SUMS", null, null, md5sum.getBytes(StandardCharsets.UTF_8), null, "text/plain", null, null)
                    ))
                    .peek(c -> c.left().appendEvent(new DomsEvent(agent, new Date(),
                            (c.right().length() - c.right().replace("\n", "").length()) + " files checksummed", eventName, true))
                    )
                    // now render the processed paths as a list to present at the end of the job
                    .map(st -> st.left().getPath())
                    .sorted()
                    .collect(toList())
                    .toString();

            return f;
        }

        /**
         * The path stored in DOMS is one level higher than the one in the Infomedia supplied checksum files.  So strip
         * up to first '/' (all ingested files have this)
         *
         * @param domsItem doms item to extract one-level-down-path from
         * @return relative path
         */
        public String checksumFilePathFor(DomsItem domsItem) {
            String pathInDoms = domsItem.getPath();
            return pathInDoms.substring(pathInDoms.indexOf('/') + 1);
        }

        /**
         * @noinspection unused, StatementWithEmptyBody
         */
        public static String md5ForClosableInputStream(InputStream originalInputStream) {
            try {
                MessageDigest digest = MessageDigest.getInstance("md5");
                try (DigestInputStream inputStream = new DigestInputStream(new BufferedInputStream(originalInputStream), digest)) {
                    byte[] buf = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buf)) > 0) {
                        // just read.
                    }
                    // http://www.dev-garden.org/2013/04/16/java-byte-array-as-a-hex-string-the-easy-way/
                    return DatatypeConverter.printHexBinary(digest.digest()).toLowerCase(Locale.ROOT);
                }
            } catch (Exception e) {
                throw new RuntimeException("md5ForInputStream()", e);
            }
        }

        /**
         * Validate all xml-contents located as child under the delivery
         *
         * @param mxBean    JMX bean to update statistics in
         * @param eventName event name to use for events stored in DOMS.
         * @return function to apply on delivery DOMS items.
         */
        private Function<DomsItem, ToolResult> processChildDomsId(DefaultToolMXBean mxBean, String eventName) {
            return (DomsItem parentDomsItem) -> {
                String deliveryName = parentDomsItem.getPath();
                long startDeliveryIngestTime = System.currentTimeMillis();
                log.info(KibanaLoggingStrings.START_DELIVERY_XML_VALIDATION_AGAINST_XSD, deliveryName);

                // Single doms item
                final String agent = RegenerateChecksumfileMain.class.getSimpleName();

                List<StreamTuple<DomsItem, Either<Exception, ToolResult>>> toolResults = parentDomsItem.allChildren()
                        .map(DomsItemTuple::create)
                        .flatMap(c -> c.flatMap(item -> { // For an individual child, process XML datastream if present.
                                    try {
                                        return item.datastreams().stream()
                                                .filter(datastream -> datastream.getId().equals("XML"))
                                                .peek(datastream -> mxBean.idsProcessed++)
                                                .peek(datastream -> mxBean.currentId = c.toString())
                                                .map(datastream -> analyzeXML(datastream))
                                                // Save individual result as event on node.
                                                .peek(eitherExceptionToolResult -> eitherExceptionToolResult.bimap(
                                                        e -> item.appendEvent(new DomsEvent(agent, new Date(), DomsItemTuple.stacktraceFor(e), eventName, false)),
                                                        tr -> item.appendEvent(new DomsEvent(agent, new Date(), tr.getHumanlyReadableMessage(), eventName, tr.isSuccess())))
                                                );
                                    } catch (Exception e) {
                                        return Stream.of(Either.left(e));
                                    }
                                }
                        ))
                        .collect(toList());

                ToolResultsReport<DomsItem> trr = new ToolResultsReport<>(new ToolResultsReport.OK_COUNT_FAIL_LIST_RENDERER<>(),
                        (id, t) -> log.error("id: {}", id, t),
                        t -> Throwables.getStackTraceAsString(t));

                ToolResult result = trr.apply(parentDomsItem, toolResults);

                long finishedDeliveryIngestTime = System.currentTimeMillis();
                log.info(KibanaLoggingStrings.FINISHED_DELIVERY_XML_VALIDATION_AGAINST_XSD, deliveryName, finishedDeliveryIngestTime - startDeliveryIngestTime);

                return result;
            };
        }

        /**
         * Start validating xml-content in fedora and return results
         *
         * @param ds DomsDatastream containing the XML file to validate.
         * @return a ToolResult indicating if it went well or not (wrapped in an Either to hold any exception).
         */
        protected Either<Exception, ToolResult> analyzeXML(DomsDatastream ds) {
            throw new RuntimeException();
//            Map<String, String> xsdMap = provideXsdRootMap();
//            // Note:  We ignore character set problems for now.
//            final String datastreamAsString = ds.getDatastreamAsString();
//
//            try {
//                final ToolResult toolResult;
//
//                String rootnameInCurrentXmlFile = getRootTagName(new InputSource(new StringReader(datastreamAsString)));
//                String xsdFile = xsdMap.get(rootnameInCurrentXmlFile);
//                if (xsdFile == null) {
//                    toolResult = ToolResult.fail("Unknown root:" + rootnameInCurrentXmlFile);
//                } else {
//                    URL url = Objects.requireNonNull(getClass().getClassLoader().getResource(xsdFile), "xsdFile not found: " + xsdFile);
//
//                    Validator validator = getValidatorFor(url);
//
//                    final StreamSource source = new StreamSource(new StringReader(datastreamAsString));
//                    validator.validate(source); // throws exception if invalid.
//                    log.trace("{}: XML valid", ds.getDomsItem().getDomsId().id());
//                    toolResult = ToolResult.ok(""); // Empty message if ok.  Makes GUI presentation smaller.
//                }
//                return Either.right(toolResult);
//            } catch (Exception e) {
//                return Either.left(e);
//            }
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }
    }
}

