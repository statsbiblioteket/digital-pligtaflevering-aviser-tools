package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ingesters;

import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.fedora.fedoraDBsearch.DBSearchRest;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Stream;

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
    private DBSearchRest dbSearchRest;
    private WebResource restApi;

    @Inject
    public DomsFilesystemIngester(DomsRepository repository,
                                  @Named(ITERATOR_FILESYSTEM_IGNOREDFILES) String ignoredFiles,
                                  DBSearchRest dbSearchRest) {
        this.repository = repository;
        this.ignoredFiles = ignoredFiles;
        this.dbSearchRest = dbSearchRest;
    }

    @Override
    public String apply(DomsId domsId, Path rootPath) {
        Set<String> ignoredFilesSet = new TreeSet<>(Arrays.asList(ignoredFiles.split(" *, *")));

        try {
            final DomsItem item = repository.lookup(domsId);

            ObjectProfile op = repository.getObjectProfile(domsId.id(), null);

            Stream.of("path:B20160811", "path:B20160811-RT1", "path:B20160811-RT2")
                    .forEach(s -> {
                        List<String> o = null;
                        try {
                            o = dbSearchRest.findObjectFromDCIdentifier(s);
                        } catch (BackendInvalidCredsException e) {
                            e.printStackTrace();
                        } catch (BackendMethodFailedException e) {
                            e.printStackTrace();
                        }
                        System.out.println(o);
                    });

            log.trace("{}", op);
            /* walk() guarantees that we have always seen the parent of a directory before we
             see the directory itself.  This mean that we can rely of the parent being in DOMS */

            Files.walk(rootPath)
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
