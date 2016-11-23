package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ingesters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 *
 */
public class IngesterFileVisitor extends SimpleFileVisitor<Path> {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected IngesterFileVisitor() {
        super();
    }

    List<Path> childrenOfCurrentDirectory = Collections.emptyList();
    Stack<List<Path>> childrenOfEarlierDirectories = new Stack<>();

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        log.trace("previsitdirectory: {}", dir);
        childrenOfEarlierDirectories.push(childrenOfCurrentDirectory);
        childrenOfCurrentDirectory = new ArrayList<>();
        // create doms object for node and add hasPart to parent.
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        log.trace("visitfile: {}", file);
        childrenOfCurrentDirectory.add(file);
        // create datastream, and add hasPart (previously in post visit) to parent
        return super.visitFile(file, attrs);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        log.trace("postvisitdirectory: {}, {}", dir, childrenOfCurrentDirectory);
        childrenOfCurrentDirectory = childrenOfEarlierDirectories.pop();
        return super.postVisitDirectory(dir, exc);
    }
}
