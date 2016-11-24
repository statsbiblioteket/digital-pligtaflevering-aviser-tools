package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ingesters;

import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_IGNOREDFILES;

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

    @Inject
    public DomsFilesystemIngester(DomsRepository repository,
                                  @Named(ITERATOR_FILESYSTEM_IGNOREDFILES) String ignoredFiles,
                                  WebResource restApi) {
        this.repository = repository;
        this.ignoredFiles = ignoredFiles;
        this.restApi = restApi;
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

            Files.walk(deliveryPath)
                    .filter(Files::isDirectory)
                    .map(path -> rootPath.relativize(path))
                    .forEach(this::createDirectoryWithDataStreamsInDoms);
        } catch (IOException e) {
            throw new RuntimeException("domsId: " + domsId + ", rootPath: " + rootPath, e);
        }
//
//        FileVisitor<Path> fv = new IngesterFileVisitor();
//
//        try {
//            Files.walkFileTree(rootPath, fv);
//        } catch (IOException e) {
//            throw new RuntimeException("domsId: " + domsId + " rootPath: " + rootPath, e);
//        }

        return null;
    }

    protected void createDirectoryWithDataStreamsInDoms(Path path) {
        log.trace("Dir: {}", path);
    }
}
