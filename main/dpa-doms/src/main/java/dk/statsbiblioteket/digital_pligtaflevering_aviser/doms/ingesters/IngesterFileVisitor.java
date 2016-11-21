package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ingesters;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 */
public class IngesterFileVisitor extends SimpleFileVisitor<Path> {

    protected IngesterFileVisitor() {
        super();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        System.out.println("previsitdirectory: " + dir);
        // create doms object for node and add hasPart to parent.
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        System.out.println("visitfile: " + file);
        // create datastream, and add hasPart (previously in post visit) to parent
        return super.visitFile(file, attrs);
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        System.out.println("postvisitdirectory: " + dir);
        return super.postVisitDirectory(dir, exc);
    }
}
