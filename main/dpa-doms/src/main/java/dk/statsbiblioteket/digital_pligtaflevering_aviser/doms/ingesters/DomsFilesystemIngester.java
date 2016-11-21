package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ingesters;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

/**
 * DomsFilesystemIngester takes a given directory and creates a corresponding set of DOMS objects.  One object for each
 * directory and one object for each file (some are ignored).  A <code>hasPart</code> relation is created between a
 * given object and the object for the parent directory it belongs to.
 */
public class DomsFilesystemIngester implements BiFunction<DomsId, Path, String> {

    private DomsRepository repository;

    @Inject
    public DomsFilesystemIngester(DomsRepository repository) {
        this.repository = repository;
    }

    @Override
    public String apply(DomsId domsId, Path rootPath) {
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
