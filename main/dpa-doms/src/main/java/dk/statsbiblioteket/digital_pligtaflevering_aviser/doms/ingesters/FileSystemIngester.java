package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ingesters;

import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.bitrepository.modify.putfile.PutFileClient;


import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_IGNOREDFILES;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;
import static dk.statsbiblioteket.newspaper.bitrepository.ingester.DomsFileUrlRegister.CONTENTS;
import static dk.statsbiblioteket.newspaper.bitrepository.ingester.DomsFileUrlRegister.RELATION_PREDICATE;
import static java.nio.file.Files.walk;

/**
 * FileSystemIngester takes a given directory and creates a corresponding set of DOMS objects.  One object for each
 * directory and one object for each file (some are ignored).  A <code>hasPart</code> relation is created between a
 * given object and the object for the parent directory it belongs to.
 */
public class FileSystemIngester implements BiFunction<DomsId, Path, String> {

    private static final String SOFTWARE_VERSION = "NAME AND VERSION OF SOFTWARE"; // FIXME

    private static final long DEFAULT_FILE_SIZE = 0;
    public static final String COLLECTIONID_PROPERTY = "bitrepository.ingester.collectionid";

    protected Logger log = LoggerFactory.getLogger(getClass());

    private DomsRepository repository;
    private String ignoredFiles;
    private PutFileClient putfileClient;
    private WebResource restApi;
    private EnhancedFedora efedora;
    private List<String> collections = Arrays.asList("doms:Newspaper_Collection"); // FIXME.
    protected Set<String> ignoredFilesSet;

    private String bitmagUrl = null;
    private String dpaIngesterId = null;

    @Inject
    public FileSystemIngester(DomsRepository repository,
                              @Named(ITERATOR_FILESYSTEM_IGNOREDFILES) String ignoredFiles,
                              @Named(BITMAG_BASEURL_PROPERTY) String bitmagUrl,
                              PutFileClient putfileClient,
                              @Named(COLLECTIONID_PROPERTY) String dpaIngesterId,
                              WebResource restApi, EnhancedFedora efedora) {
        this.repository = repository;
        this.ignoredFiles = ignoredFiles;
        this.putfileClient = putfileClient;
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

        try {
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
                throw new RuntimeException("Invalid XPath. This is a programming error.", e);
            }
            List<String> textContent = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                textContent.add(nodeList.item(i).getTextContent());
            }

            // The one starting with "path:" is the one we need.  The rest of that string is the
            // filename in the local file system.

            // ["uuid:5a06c0ed-6324-4777-86b0-075fc972dcb4", "path:B20160811-RT1"]

            String relativeFilenameFromDublinCore = textContent.stream()
                    .filter(s -> s.startsWith("path:"))
                    .map(s -> s.substring("path:".length()))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("Could not get 'path:...' identifier for " + domsId));

            // "B20160811-RT1"

            Path deliveryPath = rootPath.resolve(relativeFilenameFromDublinCore);

            if (Files.notExists(deliveryPath)) {
                throw new FileNotFoundException("Directory not found for delivery:  " + deliveryPath);
            }

            log.trace("Delivery directory: {}", deliveryPath);

            // Original in BatchMD5Validation.readChecksums()
            // 8bd4797544edfba4f50c91c917a5fc81  verapdf/udgave1/pages/20160811-verapdf-udgave1-page001.pdf

            Map<String, String> md5map = Files.lines(deliveryPath.resolve("md5sums.txt"))
                    .map(s -> s.split(" +"))
                    .collect(Collectors.toMap(a -> a[1], a -> a[0]));

            /* walk() guarantees that we have always seen the parent of a directory before we
             see the directory itself.  This mean that we can rely of the parent being in DOMS */

            // Process folders in "longest string" order:
            // dl_20160811_rt1/verapdf/articles
            // dl_20160811_rt1/verapdf/pages
            // dl_20160811_rt1/verapdf
            // dl_20160811_rt1/

            walk(deliveryPath)
                    .filter(Files::isDirectory)
                    .sorted(Comparator.reverseOrder()) // ensure children processed before parents
                    .forEach(path -> {
                        try {
                            createDirectoryWithDataStreamsInDoms("path:" + rootPath.relativize(path), rootPath, path, md5map);
                        } catch (Exception e) {
                            e.printStackTrace();  // FIXME: later
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("domsId: " + domsId + ", rootPath: " + rootPath, e);
        }

        return null;  // FIXME:  Return results list.
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

    protected void createDirectoryWithDataStreamsInDoms(String dcIdentifier, Path rootPath, Path absoluteFileSystemPath, Map<String, String> md5map) throws Exception {

        log.trace("DC id: {}", dcIdentifier);

        // see if DOMS object exist for this directory

        final String currentDirectoryPid = lookupObjectFromDCIdentifierAndCreateItIfNeeded(dcIdentifier);

        /** figure out which pages we have (for "foo/a.pdf" and "foo/a.xml" construct
         * <code> {"a" => ["foo/a.pdf", "foo/a.xml"] }</code>)
         */


        try {
            Map<String, List<Path>> pathsForPage = Files.walk(absoluteFileSystemPath, 1)
                    .filter(Files::isRegularFile)
                    .sorted()
                    .filter(path -> ignoredFilesSet.contains(path.getFileName().toString()) == false)
                    .peek(path -> log.trace("discovered regular file {}", path.getFileName()))
                    .collect(Collectors.groupingBy(path -> "path:" + basenameOfPath(rootPath.relativize(path))));

            log.trace("pathsForPage {}", pathsForPage);

            // Find the deliveryname, which is also the name of the folder where the delivery is placed
            String deliveryName = StringUtils.substringBetween(dcIdentifier, ":", "/");
            PutFileEventHandler handler = new PutFileEventHandler();

            // For each individual page create a DOMS object.  For each file in the page, consider if it is metadata or not.
            // If it is metadata store it as a datastream on the object.  If it is binary data, put it in the Bitrepository,
            // create a DOMS object for the file, store the public URL for the bitrepository file in "CONTENTS" on the file object,
            // and create a "hasFile" relation from the file group object to the page object.
            // For each "PAGEOBJECT" create a RDF ("DIRECTORYOBJECT" "HasPart" "PAGEOBJECT")-relation on "DIRECTORYOBJECT"

            List<String> pagesInThisDirectory = pathsForPage.entrySet().stream()
                    .map(entry -> {
                        final String id = entry.getKey();
                        String pageObjectId = lookupObjectFromDCIdentifierAndCreateItIfNeeded(id);
                        final List<Path> filesForPage = entry.getValue();
                        filesForPage.stream()
                                .forEach(path -> {
                                    Path deliveryPath = Paths.get(rootPath.toString(), deliveryName);
                                    Path filePath = deliveryPath.relativize(path);
                                    ChecksumDataForFileTYPE checkSum = getChecksum(md5map.get(filePath.toString()));
                                    try {
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
                                            final String mimetype = "application/pdf";  // http://stackoverflow.com/questions/51438/getting-a-files-mime-type-in-java

                                            // create DOMS object for the file
                                            String fileObjectId = lookupObjectFromDCIdentifierAndCreateItIfNeeded("path:" + rootPath.relativize(path));

                                            // save external datastream in file object.  FIXME:  Modify "application/octet-stream" to value given by KFC. path.toString() to relative path.
                                            efedora.addExternalDatastream(fileObjectId, "CONTENTS", fileId, filepathToBitmagUrl, "application/octet-stream", mimetype, null, "Adding file after bitrepository ingest " + SOFTWARE_VERSION);

                                            // Add "hasPart" relation from the page object to the file object, FIXME: KFC gives correct value for "info:fedora/fedora-system:def/relations-external#hasFile"
                                            efedora.addRelation(pageObjectId, pageObjectId, "info:fedora/fedora-system:def/relations-external#hasPart", fileObjectId, false, "linking file to page " + SOFTWARE_VERSION);

                                            // Add the checksum relation to Fedora
                                            String checksum = Base16Utils.decodeBase16(checkSum.getChecksumValue());
                                            efedora.addRelation(pageObjectId, "info:fedora/" + fileObjectId + "/" + CONTENTS, RELATION_PREDICATE, checksum, true, "Adding checksum after bitrepository ingest");

                                        } else if (path.toString().endsWith(".xml")) {
                                            // save physical bytes of XML file as "XML" data stream on page object.

                                            final String mimeType = "text/xml"; // http://stackoverflow.com/questions/51438/getting-a-files-mime-type-in-java
                                            byte[] allBytes = Files.readAllBytes(path);
                                            String md5checksum = "FIXME";
                                            efedora.modifyDatastreamByValue(pageObjectId, "XML", null, null, allBytes, null, mimeType, "From " + path, null);
                                        } else {
                                            throw new RuntimeException("path not pdf/xml: " + path);
                                        }
                                    } catch (Exception e) {
                                        throw new RuntimeException("creating hasFile relation", e);
                                    }
                                });
                        return pageObjectId;
                    }).collect(Collectors.toList());

            // All pages are now created in DOMS.  Add "hasPart" relations to directory object.

            if (pagesInThisDirectory.size() != 1) { // avoid triggering bug in FedoraRest.addRelations
                efedora.addRelations(currentDirectoryPid, currentDirectoryPid, "info:fedora/fedora-system:def/relations-external#hasPart", pagesInThisDirectory, false, "comment");
            } else {
                efedora.addRelation(currentDirectoryPid, currentDirectoryPid, "info:fedora/fedora-system:def/relations-external#hasPart", pagesInThisDirectory.get(0), false, "comment");
            }
            log.trace("directory {} pages {}", absoluteFileSystemPath, pagesInThisDirectory);
        } catch (IOException e) {
            throw new RuntimeException("directoryToBeDOMSObjectPath=" + dcIdentifier, e);
        }

        /**
         * NOTE: FedoraRest.addRelations has a bug occasionally invoking addRelation several times more than necessary.  FOr
         * instance when called with just one object id.  This leads to duplications of relations.  Therefore we loop ourselves causing
         * a new REL-EXT datastream version for every write.  See ABR for details.
         */

        // For each subdirectory in this directory, lookup the child DOMS id using its relative Path and create
        // a RDF ("DIRECTORYOBJECT" "HasPart" "CHILDOBJECT")-relation on "DIRECTORYOBJECT". This will work because the subdirectories are processed first.

        List<String> childDirectoryObjectIds = null;
        try {
            List<String> childIds = Files.walk(absoluteFileSystemPath, 1)
                    .filter(Files::isDirectory)
                    .skip(1) // Skip the parent directory itself.  FIXME:  Ensure well-definedness
                    .sorted() // Unscramble order
                    .flatMap(path -> lookupObjectFromDCIdentifier("path:" + rootPath.relativize(path)).stream().limit(1)) // zero or one id returned
                    .collect(Collectors.toList());

            if (childIds.size() != 1) { // avoid triggering bug in FedoraRest.addRelations
                efedora.addRelations(currentDirectoryPid, currentDirectoryPid, "info:fedora/fedora-system:def/relations-external#hasPart", childIds, false, "comment");
            } else {
                efedora.addRelation(currentDirectoryPid, currentDirectoryPid, "info:fedora/fedora-system:def/relations-external#hasPart", childIds.get(0), false, "comment");
            }
        } catch (Exception e) {
            throw new RuntimeException("addRelations child dirs", e);
        }
        log.trace("childDirectoryObjectIds {}", childDirectoryObjectIds);

    }

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
