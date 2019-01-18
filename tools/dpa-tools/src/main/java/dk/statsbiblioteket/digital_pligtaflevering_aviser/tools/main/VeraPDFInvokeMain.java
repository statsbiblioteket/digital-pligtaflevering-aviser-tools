package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
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
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.DomsItemTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester.KibanaLoggingStrings;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.EventTrigger;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.apache.commons.codec.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

/**
 * 'VeraPDFInvokeMain' runs verPdf of each pdf-file in a delivery and validate against pdfa-1b.
 * The result is written into each doms-item that contains a pdf-file
 */
public class VeraPDFInvokeMain {
    protected static final Logger log = LoggerFactory.getLogger(VeraPDFInvokeMain.class);

    public static final String VERAPDF_DATASTREAM_NAME = "VERAPDF";
    public static final String DPA_VERAPDF_URL = "dpa.verapdf.url";

    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.DaggerVeraPDFInvokeMain_VeraPdfTaskDaggerComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, VeraPDFInvokeModule.class, BitRepositoryModule.class})
    interface VeraPdfTaskDaggerComponent {
        Tool getTool();

    }

    interface VeraPDFInvoker extends Function<URL, String> {
    }

    /**
     * @noinspection WeakerAccess
     */
    @Module
    public static class VeraPDFInvokeModule {
        public static final String DPA_VERAPDF_REUSEEXISTINGDATASTREAM = "dpa.verapdf.reuseexistingdatastream";

        /**
         * @noinspection PointlessBooleanExpression
         */
        @Provides
//        Runnable provideRunnable(Modified_SBOIEventIndex index, DomsEventStorage<Item> domsEventStorage, Stream<EventTrigger.Query> queryStream, Task task) {
        protected Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                                   QuerySpecification workToDoQuery,
                                   DomsRepository domsRepository,
                                   @Named(BITMAG_BASEURL_PROPERTY) String bitrepositoryURLPrefix,
                                   @Named(BitRepositoryModule.BITREPOSITORY_SBPILLAR_MOUNTPOINT) String bitrepositoryMountpoint,
                                   @Named(DPA_VERAPDF_REUSEEXISTINGDATASTREAM) boolean reuseExistingDatastream,
                                   Provider<VeraPDFInvoker> veraPdfInvokerProvider,
                                   DefaultToolMXBean mxBean) {

            final String agent = getClass().getSimpleName();

            Tool tool = () -> Stream.of(workToDoQuery)

                    .flatMap(domsRepository::query)
                    .peek(o -> log.trace("Query returned: {}", o))
                    .map(item -> new StreamTuple<>(item.toString(), item))
                    .map(streamTuple -> streamTuple.map(
                            roundtripItem -> {
                                Map<Boolean, List<Either<Throwable, Boolean>>> eithersFromRoundtrip = roundtripItem.allChildren()
                                        .peek(child -> mxBean.currentId = String.valueOf(child))
                                        .peek(child -> mxBean.idsProcessed++)
                                        // ignore those already processed if flag is set
                                        .filter(child -> (child.datastreams().stream()
                                                .anyMatch(ds -> ds.getId().equals(VERAPDF_DATASTREAM_NAME)) && reuseExistingDatastream) == false)
                                        // process pdf datastreams.
                                        .flatMap(child -> child.datastreams().stream()
                                                .filter(datastream -> datastream.getMimeType().equals("application/pdf"))
                                                .peek(datastream -> log.trace("Found PDF stream on {}", child))
                                                .map(datastream -> getUrlForBitrepositoryItemPossiblyLocallyAvailable(child, bitrepositoryURLPrefix, bitrepositoryMountpoint, datastream.getUrl()))
                                                .map(url -> Try.of(() -> {
                                                    log.trace("{} - processing URL: {}", child, url);

                                                    long startTime = System.currentTimeMillis();
                                                    String veraPDF_output = veraPdfInvokerProvider.get().apply(url);
                                                    if(!isXMLLike(veraPDF_output)) {
                                                        log.error("File does not appear to be valid " + url);
                                                        throw new Exception("File does not appear to be valid " + url);
                                                    }
                                                    child.modifyDatastreamByValue(VERAPDF_DATASTREAM_NAME, null, null, veraPDF_output.getBytes(StandardCharsets.UTF_8), null, "text/xml", "URL: " + url, null);
                                                    log.info(KibanaLoggingStrings.FINISHED_FILE_PDFINVOKE, url, (System.currentTimeMillis() - startTime));
                                                    return Boolean.TRUE;
                                                }).toEither())
                                                .peek((Either<Throwable, Boolean> either) -> log.trace("{}", either)))
                                        .collect(partitioningBy(either -> either.isRight()));

                        List<Boolean> successful = eithersFromRoundtrip.get(Boolean.TRUE).stream()
                                .map(either -> either.right().get())
                                .collect(toList());
                        List<String> failed = eithersFromRoundtrip.get(Boolean.FALSE).stream()
                                .map(either -> DomsItemTuple.stacktraceFor(either.left().get()))
                                .collect(toList());

                        if (failed.size() == 0) {
                            roundtripItem.appendEvent(new DomsEvent(agent, new Date(), successful.size() + " processed", eventName, true));
                            return true;
                        } else {
                            // we have encountered exceptions not handled lower down.  Report those.
                            roundtripItem.appendEvent(new DomsEvent(agent, new Date(), String.join("\n\n", failed), eventName, false));
                            return false;
                        }
                    }))
                    .collect(toList());

            return tool;
        }

        /**
         * return true if the String passed in is something like XML
         *
         * @param inXMLStr a string that might be XML
         * @return true of the string is XML, false otherwise
         */
        public static boolean isXMLLike(String inXMLStr) {
            if (inXMLStr != null && inXMLStr.trim().length() > 0) {
                // IF WE EVEN RESEMBLE XML
                return inXMLStr.trim().startsWith("<");
            }
            return false;
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
                    if(!file.exists()) {
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
        VeraPDFInvoker getVeraPDFInvoker(@Named(DPA_VERAPDF_URL) String verapdfURL) {
            Client client = Client.create();
            WebResource webResource = client.resource(verapdfURL);
            return url -> { // multi line for breakpoints.
                final MultiPart multiPart = new FormDataMultiPart().field("url", url.toString(), MediaType.TEXT_PLAIN_TYPE);
                multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

                ClientResponse response = webResource.type("multipart/form-data").accept(MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class, multiPart);
                String responseStr = response.getEntity(String.class);
                return responseStr;
            };
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
