package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ingesters;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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

    private DomsRepository repository;
    private String ignoredFiles;

    @Inject
    public DomsFilesystemIngester(DomsRepository repository,
                                  @Named(ITERATOR_FILESYSTEM_IGNOREDFILES) String ignoredFiles) {
        this.repository = repository;
        this.ignoredFiles = ignoredFiles;
    }

    @Override
    public String apply(DomsId domsId, Path rootPath) {
        Set<String> ignoredFilesSet = new TreeSet<>(Arrays.asList(ignoredFiles.split(" *, *")));

        final DomsItem item = repository.lookup(domsId);

        FileVisitor<Path> fv = new IngesterFileVisitor();

        try {
            Files.walkFileTree(rootPath, fv);
        } catch (IOException e) {
            throw new RuntimeException("domsId: " + domsId + " rootPath: " + rootPath, e);
        }

        return null;
    }
}
