package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester;

import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.ToolResult;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.PutFileEventHandler;
import dk.statsbiblioteket.newspaper.bitrepository.ingester.NewspaperFileNameTranslater;
import dk.statsbiblioteket.util.xml.DOM;
import org.apache.commons.lang.StringUtils;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.modify.putfile.PutFileClient;
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
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_IGNOREDFILES;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;
import static dk.statsbiblioteket.newspaper.bitrepository.ingester.DomsFileUrlRegister.CONTENTS;
import static dk.statsbiblioteket.newspaper.bitrepository.ingester.DomsFileUrlRegister.RELATION_PREDICATE;
import static java.nio.file.Files.walk;

/**
 * <p> FileSystemIngester takes a given directory and creates a corresponding set of DOMS objects.  One object for each
 * directory and one object for each file (some are ignored).  A <code>hasPart</code> relation is created between a
 * given object and the object for the parent directory it belongs to. </p><p>NOTE: FedoraRest.addRelations has a bug
 * occasionally invoking addRelation several times more than necessary.  For instance when called with just one object
 * id.  This leads to duplications of relations.  Therefore we treat a single relation as a special case.
 * See ABR for details. </p>
 */
public class FileSystemDeliveryIngester implements BiFunction<DomsId, Path, String> {

    private static final String SOFTWARE_VERSION = "NAME AND VERSION OF SOFTWARE"; // FIXME

    private static final long DEFAULT_FILE_SIZE = 0;
    public static final String COLLECTIONID_PROPERTY = "bitrepository.ingester.collectionid";

    protected Logger log = LoggerFactory.getLogger(getClass());

    private DomsRepository repository;
    private String ignoredFiles;
    private PutFileClient putfileClient;
    private final String bitrepositoryUrlPrefix;
    private final String bitrepositoryMountpoint;
    private WebResource restApi;
    private EnhancedFedora efedora;
    private List<String> collections = Arrays.asList("doms:Newspaper_Collection"); // FIXME.
    protected Set<String> ignoredFilesSet;

    private String bitmagUrl = null;
    private String dpaIngesterId = null;

    @Inject
    public FileSystemDeliveryIngester(DomsRepository repository,
                                      @Named(ITERATOR_FILESYSTEM_IGNOREDFILES) String ignoredFiles,
                                      @Named(BITMAG_BASEURL_PROPERTY) String bitmagUrl,
                                      PutFileClient putfileClient,
                                      @Named(COLLECTIONID_PROPERTY) String dpaIngesterId,
                                      @Named("bitrepository.ingester.baseurl") String bitrepositoryUrlPrefix,
                                      @Named("bitrepository.sbpillar.mountpoint") String bitrepositoryMountpoint,
                                      WebResource restApi, EnhancedFedora efedora) {
        this.repository = repository;
        this.ignoredFiles = ignoredFiles;
        this.putfileClient = putfileClient;
        this.bitrepositoryUrlPrefix = bitrepositoryUrlPrefix;
        this.bitrepositoryMountpoint = bitrepositoryMountpoint;
        this.restApi = restApi;
        this.efedora = efedora;

        this.bitmagUrl = bitmagUrl;
        this.dpaIngesterId = dpaIngesterId;

        ignoredFilesSet = new TreeSet<>(Arrays.asList(ignoredFiles.split(" *, *")));
        log.trace("Ignored files: {}", ignoredFilesSet);
    }

    /**
     * For a given domsId we first have to locate the physical location of the delivery. <p>The convention is to get the
     * Dublin Core identifiers and the one that starts with "path:" contains the path of the delivery directory relative
     * to the passed in rootDir.
     * <p>
     * Then we can create the objects in DOMS corresponding to the files in the delivery directory as follows:
     * <p>
     * <ul> <li>Each directory becomes a DOMS object.</li> <li>A file group exist for all files with the same basename.
     * For "a.pdf" and "a.xml" the file group is named "a" and the corresponding DOMS object will be named "A".</li>
     * <li>Each directory DOMS object will have a "hasPart" RDF relation to the DOMS objects for the file groups and
     * directories it contains.</li> <li>For binary files in a file group, the file will be ingested in the
     * Bitrepository and a child DOMS object created with a CONTENTS datastream type "R" redirecting to the public URL
     * for the file in the Bitrepository (which for the Statsbiblioteket pillar can be transformed to be resolved as a
     * local file).  A "hasFile" relation is created from the file group object to the child object.</li> <li>For
     * non-binary metadatafiles they are stored as a managed Fedora datastream type "M" named with the extension for the
     * file.  ("a.xml" will be stored in the o.</li> </ul>
     *
     * @param domsId
     * @param rootPath
     * @return
     */
    @Override
    public String apply(DomsId domsId, Path rootPath) {
        long startTime = System.currentTimeMillis();

        // Collect all the indvidual toolResults.
        List<ToolResult> toolResult = ingestDirectoryForDomsId(domsId, rootPath);

        // Sort according to result
        final Map<Boolean, List<ToolResult>> toolResultMap = toolResult.stream()
                .collect(Collectors.groupingBy(tr -> tr.getResult()));

        List<ToolResult> failingToolResults = toolResultMap.getOrDefault(Boolean.FALSE, Collections.emptyList());

        // Combine the
        String deliveryEventMessage = failingToolResults.stream()
                .map(tr -> "---\n" + tr.getHumanlyReadableMessage() + "\n" + tr.getHumanlyReadableStackTrace())
                .filter(s -> s.trim().length() > 0) // skip blank lines
                .collect(Collectors.joining("\n"));

        // outcome was successful only if no toolResults has a FALSE result.
        boolean outcome = failingToolResults.size() == 0;

        final String keyword = getClass().getSimpleName();

        repository.appendEventToItem(domsId, keyword, new java.util.Date(), deliveryEventMessage, "Data_Archived", outcome);
        log.info("{} {} Took: {} ms", keyword, domsId, (System.currentTimeMillis() - startTime));
        return "domsID " + domsId + " ingested. outcome = " + outcome;
    }

    public List<ToolResult> ingestDirectoryForDomsId(DomsId domsId, Path rootPath) {

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

        String dcContent = restApi.path(domsId.id()).path("/datastreams/DC/content").queryParam("format", "xml").get(String.class);  // Ask directly for datastream?
        NodeList nodeList;
        try {
            nodeList = (NodeList) xPath.compile("//dc:identifier").evaluate(
                    DOM.streamToDOM(new ByteArrayInputStream(dcContent.getBytes()), true), XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            return Arrays.asList(ToolResult.fail("Invalid XPath. This is a programming error.", e));
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

        if (relativeFilenameFromDublinCore.isPresent() == false) {
            return Arrays.asList(ToolResult.fail("Could not get 'path:...' identifier for " + domsId));
        }

        Path deliveryPath = rootPath.resolve(relativeFilenameFromDublinCore.get());

        if (Files.notExists(deliveryPath)) {
            return Arrays.asList(ToolResult.fail("Directory not found for delivery:  " + deliveryPath));
        }

        log.trace("Delivery directory: {}", deliveryPath);

        // Original in BatchMD5Validation.readChecksums()
        // 8bd4797544edfba4f50c91c917a5fc81  verapdf/udgave1/pages/20160811-verapdf-udgave1-page001.pdf

        Map<String, String> md5map;
        try {
            md5map = Files.lines(deliveryPath.resolve("checksums.txt"))
                    .map(s -> s.split(" +"))
                    .collect(Collectors.toMap(a -> a[1], a -> a[0]));
        } catch (Exception e) {
            return Arrays.asList(ToolResult.fail("Could not read checksums.txt", e));
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
            pathStream = walk(deliveryPath);
        } catch (IOException e) {
            return Arrays.asList(ToolResult.fail("Could not walk " + deliveryPath, e));
        }

        // We got so far so now collect the combined results for each directory.
        List<ToolResult> subDirectoryResults = pathStream
                .filter(Files::isDirectory)
                .sorted(Comparator.reverseOrder()) // ensure children processed before parents
                .flatMap(path -> createDirectoryWithDataStreamsInDoms("path:" + rootPath.relativize(path), rootPath, path, md5map))
                .collect(Collectors.toList());

        return subDirectoryResults;
    }

    /**
     * Create a checksum-object based on a checksom-string
     * @param checksum
     * @return
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
     * For the given directory: <ul> <li>Look up DOMS object for current "path:...".  If not found, create an empty DOMS
     * object here called "DIRECTORYOBJECT" for the given directory itself.</li> <li>Create a DOMS object for each file
     * (here called "FILEOBJECT").</li> <ul> <li>Create METADATA datastream for each metadata file.</li> <li>For each
     * binary file, ingest the file in BitRepository and create CONTENTS datastream for the corresponding public
     * BitRepository URL </li> </ul> <li>For each "FILEOBJECT" create a RDF ("DIRECTORYOBJECT" "HasPart"
     * "FILEOBJECT")-relation on "DIRECTORYOBJECT" </li> <li>For each subdirectory in this directory, lookup the child
     * DOMS id using its relative Path and create a RDF ("DIRECTORYOBJECT" "HasPart" "CHILDOBJECT")-relation on
     * "DIRECTORYOBJECT". This will work because the subdirectories are processed first. </li> </ul>
     *
     * @param dcIdentifier
     * @param md5map
     */

    protected Stream<ToolResult> createDirectoryWithDataStreamsInDoms(String dcIdentifier, Path rootPath, Path absoluteFileSystemPath, Map<String, String> md5map) {

        log.trace("DC id: {}", dcIdentifier);

        // see if DOMS object exist for this directory

        final String currentDirectoryPid = lookupObjectFromDCIdentifierAndCreateItIfNeeded(dcIdentifier);

        /**
         * After careful consideration we need the Delivery Ingester to create a DOMS object pr page, which is not
         * easily derived from the directory structure in the deliveries from
         * infomedia as directories.  We therefore need to introduce an additional group by filenames.
         *
         * Figure out which pages we have (for "foo/a.pdf" and "foo/a.xml" construct
         * <code> {"a" => ["foo/a.pdf", "foo/a.xml"] }</code>)
         */

        final Stream<Path> pathsInThisDirectory;
        try {
            pathsInThisDirectory = Files.walk(absoluteFileSystemPath, 1);
        } catch (IOException e) {
            return Stream.of(ToolResult.fail("Cannot walk " + absoluteFileSystemPath, e));
        }

        Map<String, List<Path>> pathsForPage = pathsInThisDirectory
                .filter(Files::isRegularFile)
                .sorted()
                .filter(path -> ignoredFilesSet.contains(path.getFileName().toString()) == false)
                .peek(path -> log.trace("discovered regular file {}", path.getFileName()))
                .collect(Collectors.groupingBy(path -> "path:" + basenameOfPath(rootPath.relativize(path))));

        Map<String, List<Path>> sortedPathsForPage = new TreeMap<>(pathsForPage);
        log.trace("pathsForPage {}", pathsForPage);

        // Find the deliveryname, which is also the name of the folder where the delivery is placed
        String deliveryName = StringUtils.substringBetween(dcIdentifier, ":", "/");
        PutFileEventHandler handler = new PutFileEventHandler();

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
                    return filesForPage.stream()
                            .sorted()
                            .map(path -> {
                                log.trace("Page file {} for {}", path, id);
                                try {
                                    Path deliveryPath = Paths.get(rootPath.toString(), deliveryName);
                                    Path filePath = deliveryPath.relativize(path);
                                    ChecksumDataForFileTYPE checkSum = getChecksum(md5map.get(filePath.toString()));
                                    if (path.toString().endsWith(".pdf")) {

                                        // Construct the fileId with the path from the deliveryfolder to the file
                                        final String fileId = NewspaperFileNameTranslater.getFileID(Paths.get(deliveryName, filePath.toString()).toString());

                                        // Use the PutClient to ingest the file into Bitrepository
                                        putfileClient.putFile(dpaIngesterId,
                                                new URL("file:///" + path.toString()), fileId, DEFAULT_FILE_SIZE,
                                                checkSum, null, handler, null);


                                        /**
                                         * If a file is named "A.PDF" it goes in the Bitrepository by taking the following steps: <ol> <li>Clone file
                                         * template.</li> <li>Upload file to Bitrepository using BitRepositoryClient API</li> <li>Construct URL pointing to
                                         * uploaded file in Bitrepository, using text manipulation to create "http://bitfinder.statsbiblioteket.dk/<i>collection</i>"
                                         * plus "path/to/A.PDF". (NOTE: we may have to convert "/" to something else to flatten into a single file name -
                                         * KFC investigates)</li> <li>Register external datastream named "CONTENTS" in DOMS referencing the URL.</li>
                                         * <li>Create "hasFile" relation  from DOMS object for "A" (which also has "A.XML" stored as the "XML" data
                                         * stream).</li> </ol>
                                         */

                                        final String filepathToBitmagUrl = bitmagUrl + fileId; // BITMAG_BASEURL_PROPERTY
                                        final String mimetype = "application/pdf";

                                        // create DOMS object for the file
                                        String fileObjectId = lookupObjectFromDCIdentifierAndCreateItIfNeeded("path:" + rootPath.relativize(path));

                                        // save external datastream in file object.
                                        efedora.addExternalDatastream(fileObjectId, "CONTENTS", fileId, filepathToBitmagUrl, "application/octet-stream", mimetype, null, "Adding file after bitrepository ingest " + SOFTWARE_VERSION);

                                        // Add "hasPart" relation from the page object to the file object.
                                        efedora.addRelation(pageObjectId, pageObjectId, "info:fedora/fedora-system:def/relations-external#hasPart", fileObjectId, false, "linking file to page " + SOFTWARE_VERSION);

                                        // Add the checksum relation to Fedora
                                        String checksum = Base16Utils.decodeBase16(checkSum.getChecksumValue());
                                        efedora.addRelation(pageObjectId, "info:fedora/" + fileObjectId + "/" + CONTENTS, RELATION_PREDICATE, checksum, true, "Adding checksum after bitrepository ingest");


                                        return ToolResult.ok("CONTENT node added for PDF for " + path);

                                    } else if (path.toString().endsWith(".xml")) {
                                        // FIXME: check md5 checksum.
                                        // save physical bytes of XML file as "XML" data stream on page object.

                                        final String mimeType = "text/xml"; // http://stackoverflow.com/questions/51438/getting-a-files-mime-type-in-java
                                        byte[] allBytes = Files.readAllBytes(path);
                                        // String md5checksum = "FIXME";
                                        // efedora.modifyDatastreamByValue(pageObjectId, "XML", ChecksumType.MD5, md5checksum, allBytes, null, mimeType, "From " + path, null);
                                        efedora.modifyDatastreamByValue(pageObjectId, "XML", null, null, allBytes, null, mimeType, "From " + path, null);
                                        return ToolResult.ok("XML datastream added for " + path);

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
                                            return ToolResult.ok("VERAPDF datastream added for " + path);
                                        } else {
                                            return ToolResult.ok("Skipped empty .verapdf file for " + path);
                                        }
                                    } else {
                                        return ToolResult.fail("path not pdf/xml: " + path);
                                    }
                                } catch (Exception e) {
                                    return ToolResult.fail("Could not process " + path, e);
                                }
                            });
                }).collect(Collectors.toList());

        // All files now processed and created in DOMS.  If any failures so far, stop here.

        toolResultsForThisDirectory.addAll(toolResultsForFilesInThisDirectory);

        if (toolResultsForThisDirectory.stream().filter(tr -> tr.getResult() == Boolean.FALSE).findAny().isPresent()) {
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
            toolResultsForThisDirectory.add(ToolResult.ok("Added hasPart from " + currentDirectoryPid + " to " + domsIdsInThisDirectory));
        } catch (Exception e) {
            toolResultsForThisDirectory.add(ToolResult.fail("Failed to add hasPart from " + currentDirectoryPid + " to " + domsIdsInThisDirectory, e));
        }

        log.trace("directory {} pages {}", absoluteFileSystemPath, domsIdsInThisDirectory);

        /**
         */

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
            toolResultsForThisDirectory.add(ToolResult.ok("Added hasPart from " + currentDirectoryPid + " to " + childDirectoryObjectIds));
        } catch (Exception e) {
            toolResultsForThisDirectory.add(ToolResult.fail("Could not add hasPart from " + currentDirectoryPid + " to " + childDirectoryObjectIds));
        }
        log.trace("childDirectoryObjectIds {}", childDirectoryObjectIds);

        return toolResultsForThisDirectory.stream();
    }

    /**
     * Ensure that we have a valid DOMS id for the given dcIdentifier.  If it is not found, create
     * a new empty DOMS object and use that.
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
     * up to that point.  For "foo/bar.txt", return "foo/bar".
     *
     * @param path
     * @return
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
}
