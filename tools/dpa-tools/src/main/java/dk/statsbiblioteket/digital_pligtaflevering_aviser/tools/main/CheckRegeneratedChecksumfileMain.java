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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event.STOPPED_STATE;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER;
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
 * <p>Important:  Until the underlying efedora is ensured threadsafe do <it>not</it> attempt to parallize the
 * stream.</p>
 *
 * @noinspection WeakerAccess
 */
public class CheckRegeneratedChecksumfileMain {
    protected static final Logger log = LoggerFactory.getLogger(CheckRegeneratedChecksumfileMain.class);

    public static void main(String[] args) {

        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerCheckRegeneratedChecksumfileMain_CheckRegeneratedChecksumfileComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, CheckRegeneratedChecksumfileModule.class})
    interface CheckRegeneratedChecksumfileComponent {
        Tool getTool();
    }

    /**
     * @noinspection WeakerAccess, Convert2MethodRef
     */
    @Module
    protected static class CheckRegeneratedChecksumfileModule {
        Logger log = LoggerFactory.getLogger(CheckRegeneratedChecksumfileMain.class);  // short name

        /**
         * @noinspection PointlessBooleanExpression, UnnecessaryLocalVariable, unchecked
         */
        @Provides
        Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                         @Named(ITERATOR_FILESYSTEM_BATCHES_FOLDER) String deliveryFolderName,
                         QuerySpecification workToDoQuery,
                         DomsRepository domsRepository,
                         DefaultToolMXBean mxBean) {
            final String agent = CheckRegeneratedChecksumfileMain.class.getSimpleName();

            //
            Tool tool = () -> Stream.of(workToDoQuery)
                    .flatMap(domsRepository::query)
                    .peek(roundtripItem -> log.info("Checking regenerated checksums for: {}", roundtripItem))
                    .peek(roundtripItem -> mxBean.currentId = roundtripItem.getPath() + " " + roundtripItem.getDomsId().id())
                    .peek(roundtripItem -> mxBean.idsProcessed++)

                    // roundtrip node for a day delivery and we need to process all the children to get the combined md5sum.
                    .map(StreamTuple::create)
                    .map(st0 -> st0.map(roundtripItem -> Eithers.tryCatch(() -> {
                                Thread.sleep(10000);
                                String datastream = roundtripItem.datastream(RegenerateChecksumfileMain.MD5SUMS_DATASTREAM).getDatastreamAsString();
                                List<String> datastreamLines = Arrays.asList(datastream.split("\\r?\\n")); // https://stackoverflow.com/a/454913/53897
                                Set<String> datastreamSet = new HashSet<>(datastreamLines);

                                final Path path = Paths.get(deliveryFolderName, roundtripItem.getPath(), "checksums.txt");
                                if (Files.notExists(path)) {
                                    throw new IOException("Not found: " + path.toString());
                                }

                                List<String> fileLines = Files.lines(path)
                                        .map(s -> s.replaceAll("\r", "")) // File may be written on Windows.
                                        .collect(toList());
                                Set<String> fileSet = new HashSet<>(fileLines);

                                Set<String> onlyInFileSet = new HashSet<>(fileSet);
                                onlyInFileSet.removeAll(datastreamSet);

                                Set<String> onlyInDatastreamSet = new HashSet<>(datastreamSet);
                                onlyInDatastreamSet.removeAll(fileSet);

                                Set<String> onlyInOneSet = new HashSet<>(onlyInFileSet);
                                onlyInOneSet.addAll(onlyInDatastreamSet);

                                return onlyInOneSet;
                            }
                    )))
                    // stream:  RoundtripItem -> Either.right(Set<String> not common)
                    .peek((StreamTuple<DomsItem, Either<Exception, Set<String>>> st) -> log.trace("{}", st))
                    .peek(st -> mxBean.details = st.right().toString())

                    // Process result and generate "outcome=true" or "outcome=false" (if an error happened)
                    .map(st -> st.map((roundtripItem, either) ->
                                    "outcome=" + either.fold(
                                            (Exception exception) -> {
                                                log.error("{}: {}", roundtripItem.getPath(), roundtripItem.toString(), exception);
                                                roundtripItem.appendEvent(new DomsEvent(agent, new Date(), DomsItemTuple.stacktraceFor(exception), eventName, false));
                                                roundtripItem.appendEvent(new DomsEvent(agent, new Date(), "autonomous component failed", STOPPED_STATE, true));

                                                return false;
                                            },
                                            (Set<String> set) -> {
                                                if (set.size() == 0) {
                                                    roundtripItem.appendEvent(new DomsEvent(agent, new Date(), "", eventName, true));
                                                    return true;
                                                } else {
                                                    roundtripItem.appendEvent(new DomsEvent(agent, new Date(), String.join("\n", set), eventName, false));
                                                    roundtripItem.appendEvent(new DomsEvent(agent, new Date(), "checksum files don't match", STOPPED_STATE, true));
                                                    return false;
                                                }
                                            }
                                    )
                            )
                    )
                    // stream:  RoundtripItem -> "outcome=X"
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

