package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester;

import com.google.common.base.Joiner;
import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.FileNameToFileIDConverter;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.FilePathToChecksumPathConverter;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.RelativePathToURLConverter;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.ChecksumType;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.newspaper.bitrepository.ingester.utils.AutoCloseablePutFileClient;
import dk.statsbiblioteket.util.xml.DOM;
import org.apache.commons.lang.StringUtils;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.PutFileEventHandler;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper.DPA_GIT_ID;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule.PROVIDE_ENCODE_PUBLIC_URL_FOR_FILEID;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_COLLECTION;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_IGNOREDFILES;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.URL_TO_BATCH_DIR_PROPERTY;
import static java.nio.file.Files.walk;

/**
 * <p> FileSystemIngester takes a given directory and creates a corresponding set of DOMS objects. One object for each
 * directory and one object for each file (some are ignored). A <code>hasPart</code> relation is created between a given
 * object and the object for the parent directory it belongs to. </p><p> NOTE: FedoraRest.addRelations has a bug
 * occasionally invoking addRelation several times more than necessary. For instance when called with just one object
 * id. This leads to duplications of relations. Therefore we treat a single relation as a special case. See ABR for
 * details. </p>
 *
 * @noinspection WeakerAccess, ArraysAsListWithZeroOrOneArgument
 */
public class FileSystemDeliveryIngester implements BiFunction<DomsItem, Path, String>, AutoCloseable {

    private static final long DEFAULT_FILE_SIZE = 0;
    public static final String BITREPOSITORY_INGESTER_COLLECTIONID = "bitrepository.ingester.collectionid";
    private static final String RELATION_PREDICATE = "http://doms.statsbiblioteket.dk/relations/default/0/1/#hasMD5";
    private static final String CONTENTS = "CONTENTS";
    private final URL urlToBitmagBatch;

    protected Logger log = LoggerFactory.getLogger(getClass());

    private String ignoredFiles;
    protected final AutoCloseablePutFileClient putfileClient;
    protected final Function<Path, String> fileNameToFileIDConverter;
    protected final BiFunction<Path, String, String> md5Convert;
    private final String urlToBitmagBatchPath;
    private WebResource restApi;
    private EnhancedFedora efedora;
    final private List<String> collections;
    private Set<String> ignoredFilesSet;
    private final String gitId;
    private String domsCollection;
    private Settings settings;
    //private String bitmagUrl = null;
    private String bitrepositoryIngesterCollectionId = null;

    protected final OutputHandler output = new DefaultOutputHandler(getClass());
    protected final Function<String, String> encodePublicURLForFileID;
    protected final Function<Path, Stream<Path>> deliveriesForPath;
    private final Function<List<ToolResult>, String> eventMessageForToolResults;
    private final DefaultToolMXBean mxBean;
    private String status;

    @Inject
    public FileSystemDeliveryIngester(DomsRepository repository,
                                      @Named(ITERATOR_FILESYSTEM_IGNOREDFILES) String ignoredFiles,
                                      AutoCloseablePutFileClient putfileClient,
                                      FileNameToFileIDConverter fileNameToFileIDConverter,
                                      FilePathToChecksumPathConverter md5Convert,
                                      @Named(BITREPOSITORY_INGESTER_COLLECTIONID) String bitrepositoryIngesterCollectionId,
                                      @Named(URL_TO_BATCH_DIR_PROPERTY) String urlToBitmagBatchPath,
                                      @Named(DomsId.DPA_WEBRESOURCE) WebResource restApi,
                                      EnhancedFedora efedora,
                                      @Named(DPA_GIT_ID) String gitId,
                                      @Named(DOMS_COLLECTION) String domsCollection,
                                      @Named(PROVIDE_ENCODE_PUBLIC_URL_FOR_FILEID) Function<String, String> encodePublicURLForFileID,
                                      Function<Path, Stream<Path>> deliveriesForPath,
                                      Function<List<ToolResult>, String> eventMessageForToolResults,
                                      DefaultToolMXBean mxBean,
                                      Settings settings) {
        this.ignoredFiles = ignoredFiles;
        this.putfileClient = putfileClient;
        this.fileNameToFileIDConverter = fileNameToFileIDConverter;
        this.md5Convert = md5Convert;
        this.urlToBitmagBatchPath = urlToBitmagBatchPath; // file:///delivery-samples/
        try {
            this.urlToBitmagBatch = new URL(urlToBitmagBatchPath);
        } catch (MalformedURLException e) {
            throw new RuntimeException(URL_TO_BATCH_DIR_PROPERTY + "=" + urlToBitmagBatchPath + " not valid url", e);
        }

        this.restApi = restApi;
        this.efedora = efedora;
        this.encodePublicURLForFileID = encodePublicURLForFileID;
        this.deliveriesForPath = deliveriesForPath;
        this.eventMessageForToolResults = eventMessageForToolResults;
        this.mxBean = mxBean;

        //this.bitmagUrl = bitmagUrl;
        this.bitrepositoryIngesterCollectionId = bitrepositoryIngesterCollectionId;

        ignoredFilesSet = new TreeSet<>(Arrays.asList(ignoredFiles.split(" *, *")));
        this.gitId = gitId;
        this.domsCollection = domsCollection;
        collections = Arrays.asList(domsCollection); // add split if more.
        this.settings = settings;
        log.trace("Ignored files: {}", ignoredFilesSet);
    }

    /**
     * For a given domsId we first have to locate the physical location of the delivery.
     * The convention is to get the Dublin Core identifiers and the one that starts with "path:" contains the path of
     * the delivery directory relative to the passed in rootDir.
     * Then we can create the objects in DOMS corresponding to the files in the delivery directory as follows:
     * <ul> <li>Each directory becomes a DOMS object.</li> <li>A file group exist for all files with the same basename.
     * For "a.pdf" and "a.xml" the file group is named "a" and the corresponding DOMS object will be named "A".</li>
     * <li>Each directory DOMS object will have a "hasPart" RDF relation to the DOMS objects for the file groups and
     * directories it contains.</li> <li>For binary files in a file group, the file will be ingested in the
     * Bitrepository and a child DOMS object created with a CONTENTS datastream type "R" redirecting to the public URL
     * for the file in the Bitrepository (which for the Statsbiblioteket pillar can be transformed to be resolved as a
     * local file). A "hasFile" relation is created from the file group object to the child object.</li> <li>For
     * non-binary metadatafiles they are stored as a managed Fedora datastream type "M" named with the extension for the
     * file. ("a.xml" will be stored in the o.</li> </ul>
     *
     * @param domsItem item as queried in DOMS
     * @param rootPath directory where to locate the delivery
     * @return humanly readable string describing the outcome
     *
     * @noinspection Convert2MethodRef, PointlessBooleanExpression
     */
    @Override
    public String apply(DomsItem domsItem, Path rootPath) {
        long startTime = System.currentTimeMillis();

        // Collect all the indvidual toolResults.
        List<ToolResult> toolResult = ingestDirectoryForDomsItem(domsItem, rootPath);

        // Construct message to be stored on event for human consumption.
        String deliveryEventMessage = eventMessageForToolResults.apply(toolResult);

        // total outcome is successful only if all toolResults are.
        boolean outcome = toolResult.stream().allMatch(tr -> tr.isSuccess() == true);

        final String keyword = getClass().getSimpleName();

        domsItem.appendEvent(keyword, new java.util.Date(), deliveryEventMessage, "Data_Archived", outcome);
        log.info("{} {} Took: {} ms", keyword, domsItem, System.currentTimeMillis() - startTime);
        log.trace("{} message={}, outcome={}", domsItem, deliveryEventMessage, outcome);
        return "item " + domsItem + " ingested. outcome = " + outcome;
    }

    public List<ToolResult> ingestDirectoryForDomsItem(DomsItem domsItem, Path rootPath) {

        // First get identifiers from the Dublin Core XML data stream.
        //
        // <oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
        //  <dc:title>Newspaper Roundtrip</dc:title>
        //  <dc:identifier>uuid:5a06c0ed-6324-4777-86b0-075fc972dcb4</dc:identifier>
        //  <dc:identifier>path:B20160811-RT1</dc:identifier>
        //</oai_dc:dc>
        XPath xPath = XPathFactory.newInstance().newXPath();
        NamespaceContextImpl context = new NamespaceContextImpl();
        context.startPrefixMapping("dc", "http://purl.org/dc/elements/1.1/");
        xPath.setNamespaceContext(context);

        String dcContent = restApi.path(domsItem.getDomsId().id()).path("/datastreams/DC/content").queryParam("format", "xml").get(String.class);  // Ask directly for datastream?
        NodeList nodeList;
        try {
            nodeList = (NodeList) xPath.compile("//dc:identifier").evaluate(
                    DOM.streamToDOM(new ByteArrayInputStream(dcContent.getBytes(StandardCharsets.UTF_8)), true), XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            // FIXME: Rewrite using try instead of fail+exception.  It is an unexpected situation though.
            return Arrays.asList(ToolResult.fail(domsItem, "Invalid XPath. This is a programming error."));
        }
        List<String> textContent = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            textContent.add(nodeList.item(i).getTextContent());
        }

        // The one starting with "path:" is the one we need.  The rest of that string is the
        // filename in the local file system.
        // ["uuid:5a06c0ed-6324-4777-86b0-075fc972dcb4", "path:B20160811-RT1"]
        Optional<String> relativeFilenameFromDublinCore = textContent.stream()
                .filter(s -> s.startsWith("path:"))
                .map(s -> s.substring("path:".length()))
                .findAny();

        // "B20160811-RT1"
        //noinspection PointlessBooleanExpression
        if (relativeFilenameFromDublinCore.isPresent() == false) {
            throw new RuntimeException("Could not get 'path:...' identifier");
        }

        String batchName = relativeFilenameFromDublinCore.get();
        long startBatchIngestTime = System.currentTimeMillis();
        log.info(KibanaLoggingStrings.START_DELIVERY_INGEST, batchName);

        Path deliveryPath = rootPath.resolve(batchName);

        if (Files.notExists(deliveryPath)) {
            throw new RuntimeException("Directory not found for delivery:  " + deliveryPath);
        }

        log.trace("Delivery directory: {}", deliveryPath);

        status = deliveryPath.toString();

        // Original in DeliveryMD5Validation.readChecksums()
        // 8bd4797544edfba4f50c91c917a5fc81  verapdf/udgave1/pages/20160811-verapdf-udgave1-page001.pdf
        mxBean.details = "Checking checksums for " + deliveryPath;

        DeliveryMD5Validation md5validations;
        try {
            md5validations = new DeliveryMD5Validation(rootPath.toString(), "checksums.txt", md5Convert, ignoredFiles);
            md5validations.validation(batchName);
            List<String> validationResults = md5validations.getValidationResult();
            if (validationResults.size() > 0) {
                String collectiveValidationString = Joiner.on("\n").join(validationResults);
                return Arrays.asList(ToolResult.fail(domsItem, "Checksum validation failed:\n\n" + collectiveValidationString));
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not process checksums.txt", e);
        }

        /* walk() guarantees that we have always seen the parent of a directory before we
             see the directory itself.  This mean that we can rely of the parent being in DOMS */
        // Process folders in "longest string" order:
        // dl_20160811_rt1/verapdf/articles
        // dl_20160811_rt1/verapdf/pages
        // dl_20160811_rt1/verapdf
        // dl_20160811_rt1/
        final Stream<Path> pathStream;
        try {
            pathStream = walk(deliveryPath, FileVisitOption.FOLLOW_LINKS);
        } catch (IOException e) {
            return Arrays.asList(ToolResult.fail(domsItem, "Could not walk " + deliveryPath));
        }

        // We got so far so now collect the combined results for each directory.
        List<ToolResult> subDirectoryResults = pathStream
                .filter(Files::isDirectory)
                .sorted(Comparator.reverseOrder()) // ensure children processed before parents
                .flatMap(path -> createDirectoryWithDataStreamsInDoms(domsItem, "path:" + rootPath.relativize(path), rootPath, path, md5validations))
                .collect(Collectors.toList());

        long finishedBatchIngestTime = System.currentTimeMillis();
        log.info(KibanaLoggingStrings.FINISHED_DELIVERY_INGEST, batchName, finishedBatchIngestTime - startBatchIngestTime);
        return subDirectoryResults;
    }

    /**
     * Create a checksum-object based on a checksom-string
     *
     * @param checksum as a string to be base16 encoded.
     * @return ChecksumDataForFileTYPE wrapper containing MD5 as the type, base 16 encoded version of passed in
     * checksum, and "now" as the timestamp.
     */
    private ChecksumDataForFileTYPE getChecksum(String checksum) {
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumValue(Base16Utils.encodeBase16(checksum));
        checksumData.setCalculationTimestamp(CalendarUtils.getNow());
        ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(org.bitrepository.bitrepositoryelements.ChecksumType.MD5);
        checksumData.setChecksumSpec(checksumSpec);
        return checksumData;
    }

    /**
     * For the given directory: <ul> <li>Look up DOMS object for current "path:...". If not found, create an empty DOMS
     * object here called "DIRECTORYOBJECT" for the given directory itself.</li> <li>Create a DOMS object for each file
     * (here called "FILEOBJECT").</li> <ul> <li>Create METADATA datastream for each metadata file.</li> <li>For each
     * binary file, ingest the file in BitRepository and create CONTENTS datastream for the corresponding public
     * BitRepository URL </li> </ul> <li>For each "FILEOBJECT" create a RDF ("DIRECTORYOBJECT" "HasPart"
     * "FILEOBJECT")-relation on "DIRECTORYOBJECT" </li> <li>For each subdirectory in this directory, lookup the child
     * DOMS id using its relative Path and create a RDF ("DIRECTORYOBJECT" "HasPart" "CHILDOBJECT")-relation on
     * "DIRECTORYOBJECT". This will work because the subdirectories are processed first. </li> </ul>
     *
     * @param dcIdentifier DC identifier to look up object in DOMS with.
     * @param md5map       MD5 validation map.
     */
    protected Stream<ToolResult> createDirectoryWithDataStreamsInDoms(DomsItem rootDomsItem, String dcIdentifier, Path rootPath, Path absoluteFileSystemPath, DeliveryMD5Validation md5map) {

        log.trace("DC id: {}", dcIdentifier);

        // see if DOMS object exist for this directory
        final String currentDirectoryPid = lookupObjectFromDCIdentifierAndCreateItIfNeeded(dcIdentifier);

        /*
          After careful consideration we need the Delivery Ingester to create a DOMS object pr page, which is not
          easily derived from the directory structure in the deliveries from
          infomedia as directories.  We therefore need to introduce an additional group by filenames.

          Figure out which pages we have (for "foo/a.pdf" and "foo/a.xml" construct
          <code> {"a" => ["foo/a.pdf", "foo/a.xml"] }</code>)
         */
        Stream<Path> pathStream = deliveriesForPath.apply(absoluteFileSystemPath);

        //noinspection PointlessBooleanExpression
        Map<String, List<Path>> pathsForPage = pathStream
                .sorted()
                .filter(path -> ignoredFilesSet.contains(path.getFileName().toString()) == false)
                .peek(path -> log.trace("discovered regular file {}", path.getFileName()))
                .collect(Collectors.groupingBy(path -> "path:" + basenameOfPath(rootPath.relativize(path))));

        Map<String, List<Path>> sortedPathsForPage = new TreeMap<>(pathsForPage);
        log.trace("pathsForPage {}", pathsForPage);

        // Find the deliveryname, which is also the name of the folder where the delivery is placed
        String deliveryName = StringUtils.substringBetween(dcIdentifier, ":", "/");

        // For each individual page create a DOMS object.  For each file in the page, consider if it is metadata or not.
        // If it is metadata store it as a datastream on the object.  If it is binary data, put it in the Bitrepository,
        // create a DOMS object for the file, store the public URL for the bitrepository file in "CONTENTS" on the file object,
        // and create a "hasFile" relation from the file group object to the page object.
        // For each "PAGEOBJECT" create a RDF ("DIRECTORYOBJECT" "HasPart" "PAGEOBJECT")-relation on "DIRECTORYOBJECT"
        List<ToolResult> toolResultsForThisDirectory = new ArrayList<>();

        List<ToolResult> toolResultsForFilesInThisDirectory = sortedPathsForPage.entrySet().stream()
                .flatMap(entry -> {
                    final String id = entry.getKey();
                    String pageObjectId = lookupObjectFromDCIdentifierAndCreateItIfNeeded(id);
                    final List<Path> filesForPage = entry.getValue();
                    mxBean.details = "Page " + id + " - " + filesForPage.size() + " files";
                    return filesForPage.stream()
                            .sorted()
                            .map(path -> {
                                log.trace("Page file {} for {}", path, id);
                                mxBean.idsProcessed++;
                                try {
                                    Path deliveryPath = Paths.get(rootPath.toString(), deliveryName);
                                    Path filePath = deliveryPath.relativize(path);
                                    ChecksumDataForFileTYPE checkSum = getChecksum(md5map.getChecksum(path.getFileName().toString()));
                                    String expectedChecksum = Base16Utils.decodeBase16(checkSum.getChecksumValue());
                                    if (path.toString().endsWith(".pdf")) {
                                        //Save *.pdf files to bitrepository
                                        long startFileIngestTime = System.currentTimeMillis();
                                        log.info(KibanaLoggingStrings.START_PDF_FILE_INGEST, path);
                                        Path relativePath = rootPath.relativize(path);

                                        //Construct a synchronous eventhandler
                                        CompleteEventAwaiter eventHandler = new PutFileEventHandler(settings, output, false);

                                        String fileId = fileNameToFileIDConverter.apply(Paths.get(deliveryName, filePath.toString()));

                                        final URL urlWhereBitrepositoryCanDownloadTheFile = new RelativePathToURLConverter(urlToBitmagBatch).apply(relativePath);

                                        // Use the PutClient to ingest the file into Bitrepository
                                        // The [referenceben] does not support '/' in fileid, this mean that in development, we can only run with a teststub af putFileClient
                                        // Checksum is not validated since the bitrepository return an error if the checksum is not validated
                                        putfileClient.putFile(bitrepositoryIngesterCollectionId,
                                                urlWhereBitrepositoryCanDownloadTheFile, fileId, DEFAULT_FILE_SIZE,
                                                checkSum, null, eventHandler, null);

                                        OperationEvent finalEvent = eventHandler.getFinish();
                                        long finishedFileIngestTime = System.currentTimeMillis();
                                        log.info(KibanaLoggingStrings.FINISHED_PDF_FILE_INGEST, path, finishedFileIngestTime - startFileIngestTime);
                                        //noinspection UnnecessaryLocalVariable
                                        ToolResult toolResult = writeResultFromBitmagIngest(rootDomsItem, relativePath, finalEvent, pageObjectId, expectedChecksum);
                                        return toolResult;

                                    } else if (path.toString().endsWith(".xml")) {
                                        // save physical bytes of XML file as "XML" data stream on page object.

                                        //Start reading md5 checksum from the file before stating the ingest, if the checksum is invalid the file must not be ingested
                                        FileInputStream fis = new FileInputStream(path.toFile());
                                        String calculatedChecksum = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
                                        fis.close();
                                        if (calculatedChecksum.equals(expectedChecksum)) {//If checksum is validated start ingesting otherwise return fail
                                            final String mimeType = "text/xml"; // http://stackoverflow.com/questions/51438/getting-a-files-mime-type-in-java
                                            byte[] allBytes = Files.readAllBytes(path);
                                            efedora.modifyDatastreamByValue(pageObjectId, "XML", ChecksumType.MD5, expectedChecksum, allBytes, null, mimeType, "From " + path, null);
                                            return ToolResult.ok(rootDomsItem, "XML datastream added for " + path);
                                        } else {
                                            return ToolResult.fail(rootDomsItem, "checksum fail.  Expected " + expectedChecksum
                                                    + ", found: " + calculatedChecksum + " for " + path);
                                        }
                                    } else if (path.toString().endsWith(".verapdf")) {
                                        // VeraPDF is so slow that we accept an externally precomputed copy during ingest.  Not provided by infomedia.
                                        // save physical bytes of VERAPDF file as "VERAPDF" data stream on PDF file object.
                                        String thisDCIdentifier = "path:" + rootPath.relativize(path);
                                        String fileDCIdentifier = thisDCIdentifier.replaceFirst("\\.verapdf", ".pdf");
                                        String fileObjectId = lookupObjectFromDCIdentifierAndCreateItIfNeeded(fileDCIdentifier);

                                        final String mimeType = "text/xml";
                                        byte[] allBytes = Files.readAllBytes(path);
                                        if (allBytes.length > 0) {
                                            efedora.modifyDatastreamByValue(fileObjectId, "VERAPDF", null, null, allBytes, null, mimeType, "From " + path, null);
                                            return ToolResult.ok(rootDomsItem, "VERAPDF datastream added for " + path);
                                        } else {
                                            return ToolResult.ok(rootDomsItem, "Skipped empty .verapdf file for " + path);
                                        }
                                    } else {
                                        return ToolResult.fail(rootDomsItem, "path not pdf/xml: " + path);
                                    }
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    throw new RuntimeException(rootDomsItem + " Could not process " + path);
                                }
                            });
                }).collect(Collectors.toList());

        // All files now processed and created in DOMS.  If any failures so far, stop here.
        toolResultsForThisDirectory.addAll(toolResultsForFilesInThisDirectory);

        if (toolResultsForThisDirectory.stream().allMatch(ToolResult::isSuccess)) {
            log.trace("All successful.  Adding relations.");
        } else {
            // a failure has happened up til now, return now for cleanest error messages
            return toolResultsForThisDirectory.stream();
        }

        // Add "hasPart" relations to all domsIds on this page directory object.
        List<String> domsIdsInThisDirectory = sortedPathsForPage.keySet().stream().map(id -> lookupObjectFromDCIdentifier(id).get(0)).collect(Collectors.toList());
        try {
            if (domsIdsInThisDirectory.size() != 1) { // avoid triggering bug in FedoraRest.addRelations
                efedora.addRelations(currentDirectoryPid, currentDirectoryPid, "info:fedora/fedora-system:def/relations-external#hasPart", domsIdsInThisDirectory, false, "comment");
            } else {
                efedora.addRelation(currentDirectoryPid, currentDirectoryPid, "info:fedora/fedora-system:def/relations-external#hasPart", domsIdsInThisDirectory.get(0), false, "comment");
            }
            toolResultsForThisDirectory.add(ToolResult.ok(rootDomsItem, "Added hasPart from " + currentDirectoryPid + " to " + domsIdsInThisDirectory));
        } catch (Exception e) {
            throw new RuntimeException(rootDomsItem + " Failed to add hasPart from " + currentDirectoryPid + " to " + domsIdsInThisDirectory, e);
        }

        log.trace("directory {} pages {}", absoluteFileSystemPath, domsIdsInThisDirectory);

        // For each subdirectory in this directory, lookup the child DOMS id using its relative Path and create
        // a RDF ("DIRECTORYOBJECT" "HasPart" "CHILDOBJECT")-relation on "DIRECTORYOBJECT". This will work because the subdirectories are processed first.
        List<String> childDirectoryObjectIds = null;
        try {
            childDirectoryObjectIds = Files.walk(absoluteFileSystemPath, 1)
                    .filter(Files::isDirectory)
                    .skip(1) // Skip the parent directory itself.  FIXME:  Ensure well-definedness
                    .sorted() // Unscramble order
                    .flatMap(path -> lookupObjectFromDCIdentifier("path:" + rootPath.relativize(path)).stream().limit(1)) // zero or one id returned
                    .collect(Collectors.toList());

            if (childDirectoryObjectIds.size() != 1) { // avoid triggering bug in FedoraRest.addRelations
                efedora.addRelations(currentDirectoryPid, currentDirectoryPid, "info:fedora/fedora-system:def/relations-external#hasPart", childDirectoryObjectIds, false, "comment");
            } else {
                efedora.addRelation(currentDirectoryPid, currentDirectoryPid, "info:fedora/fedora-system:def/relations-external#hasPart", childDirectoryObjectIds.get(0), false, "comment");
            }
            toolResultsForThisDirectory.add(ToolResult.ok(rootDomsItem, "Added hasPart from " + currentDirectoryPid + " to " + childDirectoryObjectIds));
        } catch (Exception e) {
            throw new RuntimeException(rootDomsItem + " Could not add hasPart from " + currentDirectoryPid + " to " + childDirectoryObjectIds);
        }
        log.trace("childDirectoryObjectIds {}", childDirectoryObjectIds);

        log.trace("DC id: {}", dcIdentifier);

        return toolResultsForThisDirectory.stream();
    }

    /**
     * Write the result of activate putfileClient to envoke bitmagasin
     *
     * @param relativePath The [relative to deliveryfolder] path to the file
     * @param finalEvent   The event that is fetched from the putFileClient
     * @param pageObjectId pageObjectId to write to fedora
     * @param checkSum     The checksum of the file
     * @return A result of the specifik job
     */
    private ToolResult writeResultFromBitmagIngest(DomsItem rootDomsItem, Path relativePath, OperationEvent finalEvent, String pageObjectId, String checkSum) {
        //Writing to components has been inspired by the code i DomsJP2FileUrlRegister.register() from the project "dpa-bitrepository-ingester"
        final String mimetype = "application/pdf";
        ToolResult toolResult = null;

        // create DOMS object for the file
        String fileObjectId = lookupObjectFromDCIdentifierAndCreateItIfNeeded("path:" + relativePath.toString());

        switch (finalEvent.getEventType()) {
            case COMPLETE:
                try {
                    // The URLEncoding needs to be done twice to get it stored correctly inside Fedora, this is not the best solution but it is the only possible solution when running with this fedora client
                    String linkFromFedoraToBitrepository = encodePublicURLForFileID.apply(finalEvent.getFileID());

                    // save external datastream in file object.
                    efedora.addExternalDatastream(fileObjectId, "CONTENTS", finalEvent.getFileID(), linkFromFedoraToBitrepository, "application/octet-stream", mimetype, null, "Adding file after bitrepository ingest " + gitId);

                    // Add "hasPart" relation from the page object to the file object.
                    efedora.addRelation(pageObjectId, pageObjectId, "info:fedora/fedora-system:def/relations-external#hasPart", fileObjectId, false, "linking file to page " + gitId);

                    // Add the checksum relation to Fedora
                    efedora.addRelation(pageObjectId, "info:fedora/" + fileObjectId + "/" + CONTENTS, RELATION_PREDICATE, checkSum, true, "Adding checksum after bitrepository ingest");

                    toolResult = ToolResult.ok(rootDomsItem, "CONTENT node added for PDF for " + pageObjectId);
                    log.info("Completed ingest of file " + finalEvent.getFileID());

                } catch (BackendInvalidCredsException | BackendMethodFailedException | BackendInvalidResourceException e) {
                    log.error("ObjectId: " + fileObjectId + " relativePath: " + relativePath.toString(), e);
                    throw new RuntimeException("Could not process " + finalEvent.getFileID(), e);
                }
                break;
            case FAILED:
                log.info("Failed to find PutJob for file '{}' for event '{}', skipping further handling", finalEvent.getFileID(), finalEvent.getEventType());
                toolResult = ToolResult.fail(rootDomsItem, "Bitrepository failed for " + finalEvent.getFileID());
                break;
            default:
                log.debug("Got an event that I really don't care about, event type: '{}' for fileID '{}'", finalEvent.getEventType(), finalEvent.getFileID());
                break;
        }
        return toolResult;
    }

    /**
     * Ensure that we have a valid DOMS id for the given dcIdentifier. If it is not found, create a new empty DOMS
     * object and use that.
     *
     * @param dcIdentifier identifier to lookup in DOMS.
     * @return an existing DOMS id.
     */
    protected String lookupObjectFromDCIdentifierAndCreateItIfNeeded(String dcIdentifier) {

        List<String> founds = lookupObjectFromDCIdentifier(dcIdentifier);

        if (founds.isEmpty()) {
            // no DOMS object present already, create one.
            String logMessage = "Created object for " + dcIdentifier;
            try {
                final String directoryObjectPid = efedora.newEmptyObject(Arrays.asList(dcIdentifier), collections, logMessage);
                log.trace(logMessage + " / " + directoryObjectPid);
                return directoryObjectPid;
            } catch (BackendInvalidCredsException | BackendMethodFailedException | PIDGeneratorException e) {
                throw new RuntimeException("newEmptyObject() dcIdentifier=" + dcIdentifier, e);
            }
        } else {
            return founds.get(0);
        }
    }

    /**
     * Return the basename of the given path, by converting to a string, locating the last "." and returning the string
     * up to that point. For "foo/bar.txt", return "foo/bar".
     *
     * @param path path to find basename for
     * @return basename for path
     */
    protected String basenameOfPath(Path path) {
        String s = path.toString();
        int extensionPosition = s.lastIndexOf(".");
        if (extensionPosition == -1) {
            return s;
        }
        return s.substring(0, extensionPosition);
    }

    protected List<String> lookupObjectFromDCIdentifier(String dcIdentifier) {
        try {
            return efedora.findObjectFromDCIdentifier(dcIdentifier);
        } catch (BackendInvalidCredsException | BackendMethodFailedException e) {
            throw new RuntimeException("findObjectFromDCIdentifier id=" + dcIdentifier, e);
        }
    }

    @Override
    public void close() throws Exception {
        if (putfileClient != null) {
            putfileClient.close();
        }
    }
}
