package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.kb.stream.StreamTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.DomsItemTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.Eithers;
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
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event.STOPPED_STATE;
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
 * <p>The roundtripItem is traversed and exceptions+resultvalues captured in an Either which is then used to determine
 * if the roundtrip is successful or not (with the appropriate events saved on the item)</p>
 *
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
            Tool tool = () -> Stream.of(workToDoQuery)
                    .flatMap(domsRepository::query)
                    .peek(domsItem -> log.trace("Processing: {}", domsItem))
                    .peek(domsItem -> mxBean.currentId = domsItem.getPath() + " " + domsItem.getDomsId().id())

                    // roundtrip node for a day delivery and we need to process all the children to get the combined md5sum.
                    .map(StreamTuple::create)
                    .map(st0 -> st0.map(roundtripItem -> Eithers.tryCatch(
                            () -> roundtripItem.children()
                                    .flatMap(paperItem -> Stream.concat(
                                            // each paper has "pages" and "articles" subnodes.  concat the processing of both, until a nice way of splitting a stream is found.
                                            paperItem.children()
                                                    .filter(paperPartItem -> paperPartItem.getPath().endsWith("pages"))
                                                    // each page has XML datastream and a subnode with an external link to bit repo to the PDF file
                                                    .flatMap(pagesItem -> pagesItem.children()
                                                            .flatMap(pageItem ->
                                                                    Stream.concat( // #1
                                                                            Stream.of(md5sumTupleForDataStream(pageItem, "XML", md5sumFilePathFor(pageItem) + ".xml"))
                                                                            ,
                                                                            pageItem.children() // #2
                                                                                    .map(pdfItem -> md5sumTupleForDataStream(pdfItem, "CONTENTS", md5sumFilePathFor(pdfItem)))
                                                                    )
                                                            )
                                                    )
                                            ,
                                            paperItem.children() // #3
                                                    .filter(paperPartItem -> paperPartItem.getPath().endsWith("articles"))
                                                    .flatMap(articlesItem -> articlesItem.children()
                                                            .map(articleItem -> md5sumTupleForDataStream(articleItem, "XML", md5sumFilePathFor(articleItem) + ".xml"))
                                                    )
                                            )
                                    )
                                    .peek(st -> st.peek((checksummedItem, md5sumLine) -> log.trace("{}->{}", checksummedItem, md5sumLine)))
                                    .map(st -> st.right())
                                    .peek(md5sumLine -> mxBean.details = md5sumLine)
                                    .peek(md5sumLine -> mxBean.idsProcessed++)
                                    .collect(Collectors.joining("\n")) // collect all generated md5sum lines into a checksums.txt compatible "file"
                            )
                    ))
                    // stream:  DomsItem -> md5sumLines
                    .peek((StreamTuple<DomsItem, Either<Exception, String>> i) -> log.trace("{}", i))

                    .map(st -> st.map((either) -> either.map((String md5sumLines) -> md5sumLines + "\n")))  // we need a trailing newline to be compatible with md5sum output

                    .map(st -> st.map((roundtripItem, either) ->
                                    "outcome=" + either.fold(
                                            (Exception exception) -> {
                                                log.error("{}: {}", roundtripItem.getPath(), roundtripItem.toString(), exception);
                                                roundtripItem.appendEvent(new DomsEvent(agent, new Date(), DomsItemTuple.stacktraceFor(exception), eventName, false));
                                                roundtripItem.appendEvent(new DomsEvent(agent, new Date(), "autonomous component failed", STOPPED_STATE, true));

                                                return false;
                                            },
                                            (String md5sumFile) -> {
                                                byte[] bytes = md5sumFile.getBytes(StandardCharsets.UTF_8);
                                                roundtripItem.modifyDatastreamByValue("MD5SUMS", null, null, bytes, null, "text/plain", null, null);

                                                // int newlines = md5sumFile.replaceAll("[^\n]*","").length();
                                                int newlines = md5sumFile.length() - md5sumFile.replace("\n", "").length();
                                                roundtripItem.appendEvent(new DomsEvent(agent, new Date(), newlines + " files checksummed", eventName, true));
                                                return true;
                                            }
                                    )
                            )
                    )
                    .peek((StreamTuple<DomsItem, String> st) -> log.trace("{}", st))
                    .map(st -> st.map((roundtripItem, outcomeString) -> roundtripItem.getPath() + " " + roundtripItem.getDomsId().id() + " " + outcomeString))
                    .sorted()
                    .collect(toList())
                    .toString();

            return tool;
        }

        /**
         * The path stored in DOMS is one level higher than the one in the Infomedia supplied checksum files.  So strip
         * up to first '/' (all ingested files have this)
         *
         * @param domsItem doms item to extract one-level-down-path from
         * @return relative path
         */
        public String md5sumFilePathFor(DomsItem domsItem) {
            String pathInDoms = domsItem.getPath();
            return pathInDoms.substring(pathInDoms.indexOf('/') + 1);
        }

        public static DomsItemTuple<String> md5sumTupleForDataStream(DomsItem item, String datastreamName, String filename) {
            return new DomsItemTuple<>(item, md5ForClosableInputStream(item.getDataStreamInputStream(datastreamName), filename) + "  " + filename);
        }

        /**
         * @noinspection unused, StatementWithEmptyBody
         */
        public static String md5ForClosableInputStream(InputStream originalInputStream, String id) {
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
                throw new RuntimeException("md5ForInputStream() id=" + id, e);
            }
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }
    }
}

