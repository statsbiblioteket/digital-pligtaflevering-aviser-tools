package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ingesters;

import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import dk.statsbiblioteket.util.xml.DOM;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_IGNOREDFILES;
import static java.nio.file.Files.walk;

/**
 * DomsFilesystemIngester takes a given directory and creates a corresponding set of DOMS objects.  One object for each
 * directory and one object for each file (some are ignored).  A <code>hasPart</code> relation is created between a
 * given object and the object for the parent directory it belongs to.
 */
public class DomsFilesystemIngester implements BiFunction<DomsId, Path, String> {

    Logger log = LoggerFactory.getLogger(getClass());

    private DomsRepository repository;
    private String ignoredFiles;
    private WebResource restApi;
    private EnhancedFedora efedora;
    private List<String> collections = Arrays.asList("doms:Newspaper_Collection"); // FIXME.

    @Inject
    public DomsFilesystemIngester(DomsRepository repository,
                                  @Named(ITERATOR_FILESYSTEM_IGNOREDFILES) String ignoredFiles,
                                  WebResource restApi, EnhancedFedora efedora) {
        this.repository = repository;
        this.ignoredFiles = ignoredFiles;
        this.restApi = restApi;
        this.efedora = efedora;
    }

    /**
     * Two tasks:
     * <p>
     * Create a DOMS tree corresponding to the file tree as follows:
     * <ul>
     * <li>Each directory becomes a DOMS object.</li>
     * <li>Each directory DOMS object will have a "hasPart" RDF relation to the
     * DOMS objects for the files and directories it contains.</li>
     * <li>Each file becomes a DOMS object with a datastream named "CONTENTS".</li>
     * <li>For binary files, the file will be ingested in the Bitrepository and
     * "CONTENTS" will be a Fedora datastream type "R" redirecting to
     * the public URL for the file in the Bitrepository (which for the Statsbiblioteket pillar can
     * be transformed to be resolved as a local file).</li>
     * <li>For non-binary metadatafiles "CONTENTS" will be a managed Fedora datastream type "M".</li>
     * </ul>
     *
     * @param domsId
     * @param rootPath
     * @return
     */
    @Override
    public String apply(DomsId domsId, Path rootPath) {
        Set<String> ignoredFilesSet = new TreeSet<>(Arrays.asList(ignoredFiles.split(" *, *")));

        try {
            final DomsItem item = repository.lookup(domsId);

            ObjectProfile op = repository.getObjectProfile(domsId.id(), null);

            //
            XPath xPath = XPathFactory.newInstance().newXPath();
            NamespaceContextImpl context = new NamespaceContextImpl();
            context.startPrefixMapping("dc", "http://purl.org/dc/elements/1.1/");
            xPath.setNamespaceContext(context);
            XPathExpression dcIdentifierXpath = null;
            try {
                dcIdentifierXpath = xPath.compile("//dc:identifier");
            } catch (XPathExpressionException e) {
                throw new RuntimeException("xpath", e);
            }

            String dcContent = restApi.path(domsId.id()).path("/datastreams/DC/content").queryParam("format", "xml").get(String.class);
            NodeList nodeList;
            try {
                nodeList = (NodeList) dcIdentifierXpath.evaluate(
                        DOM.streamToDOM(new ByteArrayInputStream(dcContent.getBytes()), true), XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                throw new RuntimeException("Invalid XPath. This is a programming error.", e);
            }
            List<String> textContent = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                textContent.add(nodeList.item(i).getTextContent());
            }
            //
            Optional<String> dcpath = textContent.stream()
                    .filter(s -> s.startsWith("path:"))
                    .map(s -> s.substring("path:".length()))
                    .findAny();

            Path deliveryPath = rootPath.resolve(dcpath.get());

            log.trace("{}", deliveryPath);

            /* walk() guarantees that we have always seen the parent of a directory before we
             see the directory itself.  This mean that we can rely of the parent being in DOMS */

            walk(deliveryPath)
                    .filter(Files::isDirectory)
                    .sorted(Comparator.reverseOrder()) // ensure children processed before parents
                    .forEach(path -> createDirectoryWithDataStreamsInDoms("path:" + rootPath.relativize(path), rootPath, path));
        } catch (IOException e) {
            throw new RuntimeException("domsId: " + domsId + ", rootPath: " + rootPath, e);
        }

        return null;
    }

    /**
     * For the given directory: <ul> <li>Look up DOMS object for current "path:...".  If not found, create an empty DOMS
     * object here called "DIRECTORYOBJECT" for the given directory itself.</li> <li>Create a DOMS object for each file
     * (here called "FILEOBJECT").</li> <ul> <li>Create CONTENTS datastream for each metadata file.</li> <li>For each
     * binary file, ingest the file in BitRepository and create CONTENTS datastream for the corresponding public
     * BitRepository URL </li> </ul> <li>For each "FILEOBJECT" create a RDF ("DIRECTORYOBJECT" "HasPart"
     * "FILEOBJECT")-relation on "DIRECTORYOBJECT" </li> <li>For each subdirectory in this directory, lookup the child
     * DOMS id using its relative Path and create a RDF ("DIRECTORYOBJECT" "HasPart" "CHILDOBJECT")-relation on
     * "DIRECTORYOBJECT". This will work because the subdirectories are processed first. </li> </ul>
     *
     * @param dcIdentifier
     */

    protected void createDirectoryWithDataStreamsInDoms(String dcIdentifier, Path rootPath, Path absoluteFileSystemPath) {

        log.trace("Dir: {}", dcIdentifier);

        // see if DOMS object exist for this directory

        List<String> founds = lookupObjectFromDCIdentifier(dcIdentifier);

        final String directoryObjectPid; // "uuid:...."
        if (founds.isEmpty()) {
            // no DOMS object present already, create one.
            ArrayList<String> oldIds = new ArrayList<>();
            oldIds.add(dcIdentifier);
            String logMessage = "Created object for directory " + dcIdentifier;
            try {
                directoryObjectPid = efedora.newEmptyObject(oldIds, collections, logMessage);
            } catch (BackendInvalidCredsException | BackendMethodFailedException | PIDGeneratorException e) {
                throw new RuntimeException("newEmptyObject() oldIds=" + oldIds, e);
            }
            log.trace(logMessage + " / " + directoryObjectPid);
        } else {
            directoryObjectPid = founds.get(0);
        }

        // for each file in directory, create a child object.

        List<String> childFileObjectIds;
        try {
            childFileObjectIds = Files.walk(absoluteFileSystemPath, 1)
                    .filter(Files::isRegularFile)
                    .map(path -> createFileObjectInDOMS("path:" + rootPath.relativize(path), path))
                    .collect(Collectors.toList());
            log.trace("childFileObjectIds {}", childFileObjectIds);
        } catch (IOException e) {
            throw new RuntimeException("directoryToBeDOMSObjectPath=" + dcIdentifier, e);
        }

        // For each "FILEOBJECT" create a RDF ("DIRECTORYOBJECT" "HasPart" "FILEOBJECT")-relation on "DIRECTORYOBJECT"

        childFileObjectIds.stream().forEach(id -> {
            try {
                efedora.addRelation(directoryObjectPid, directoryObjectPid, "info:fedora/fedora-system:def/relations-external#hasPart", id, false, "comment");
            } catch (BackendInvalidCredsException | BackendMethodFailedException | BackendInvalidResourceException e) {
                throw new RuntimeException("addRelations " + childFileObjectIds, e);
            }
        });

        /**
         * NOTE: FedoraRest.addRelations has a bug occasionally invoking addRelation several times more than necessary.  FOr
         * instance when called with just one object id.  This leads to duplications of relations.  Therefore we loop ourselves causing
         * a new REL-EXT datastream version for every write.  See ABR for details.
         */

        // For each subdirectory in this directory, lookup the child DOMS id using its relative Path and create
        // a RDF ("DIRECTORYOBJECT" "HasPart" "CHILDOBJECT")-relation on "DIRECTORYOBJECT". This will work because the subdirectories are processed first.

        List<String> childDirectoryObjectIds = null;
        try {
            Files.walk(absoluteFileSystemPath, 1)
                    .skip(1) // Skip the parent directory itself.
                    .filter(Files::isDirectory)
                    .flatMap(path -> lookupObjectFromDCIdentifier("path:" + rootPath.relativize(path)).stream().limit(1)) // HACK!
                    .forEach(id -> {
                        try {
                            efedora.addRelation(directoryObjectPid, directoryObjectPid, "info:fedora/fedora-system:def/relations-external#hasPart", id, false, "comment");
                        } catch (Exception e) {
                            throw new RuntimeException("addRelations child dirs", e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("addRelations child dirs", e);
        }
        log.trace("childDirectoryObjectIds {}", childDirectoryObjectIds);

    }

    protected List<String> lookupObjectFromDCIdentifier(String dcIdentifier) {
        try {
            return efedora.findObjectFromDCIdentifier(dcIdentifier);
        } catch (BackendInvalidCredsException | BackendMethodFailedException e) {
            throw new RuntimeException("findObjectFromDCIdentifier id=" + dcIdentifier, e);
        }
    }

    protected String createFileObjectInDOMS(String dcIdentifier, Path domsFileObjectPath) {
        log.trace("createFileObjectInDoms {},{}", dcIdentifier, domsFileObjectPath);
        List<String> founds = lookupObjectFromDCIdentifier(dcIdentifier);
        final String fileObjectId;
        if (founds.isEmpty()) {
            // no DOMS object present already, create one.
            ArrayList<String> oldIds = new ArrayList<>();
            oldIds.add(dcIdentifier);
            String logMessage = "Created object for file " + domsFileObjectPath.toString();

            try {
                fileObjectId = efedora.newEmptyObject(oldIds, collections, logMessage);
            } catch (BackendInvalidCredsException | BackendMethodFailedException | PIDGeneratorException e) {
                throw new RuntimeException("newEmptyObject() oldIds=" + oldIds, e);
            }
        } else {
            fileObjectId = founds.get(0);
        }

        boolean goesInBitrepository = domsFileObjectPath.toString().endsWith(".pdf");  // FIXME:  Configurable

        String comment = "Added datastream for file " + domsFileObjectPath.toString();

        if (goesInBitrepository) {
            // FIXME!
        } else {
            if (domsFileObjectPath.toString().endsWith(".xml")) { // FIXME
                final String mimeType = "text/xml"; // FIXME:  text/plain for others
                final byte[] allBytes;
                try {
                    allBytes = Files.readAllBytes(domsFileObjectPath);
                } catch (IOException e) {
                    throw new RuntimeException("reading " + domsFileObjectPath, e);
                }
                try {
                    efedora.modifyDatastreamByValue(
                            fileObjectId,
                            "CONTENTS",
                            null, // no checksum
                            null, // no checksum
                            allBytes,
                            null,
                            mimeType,
                            comment,
                            null);
                } catch (BackendMethodFailedException | BackendInvalidCredsException | BackendInvalidResourceException e) {
                    throw new RuntimeException("modifying datastream for " + domsFileObjectPath, e);
                }
            }
        }
        return fileObjectId;
    }
}
