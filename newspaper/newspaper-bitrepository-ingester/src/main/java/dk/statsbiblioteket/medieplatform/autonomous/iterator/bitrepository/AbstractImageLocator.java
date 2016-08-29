package dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;

/**
 * Contain the generic functionality for the <code>IngestableFileLocator</code> defining how to run through a
 * tree finding the files to archive. Template methods are declared defining the operations the concrete
 * classes needs to implement to create a working <code>IngestableFileLocator</code>.
 */
public abstract class AbstractImageLocator implements IngestableFileLocator {
    private final TreeIterator treeIterator;

    public AbstractImageLocator(TreeIterator treeIterator) {
        this.treeIterator = treeIterator;
    }

    @Override
    public IngestableFile nextFile() {
        while (treeIterator.hasNext()) {
            ParsingEvent event = treeIterator.next();
            if (isIngestableNode(event)) {
                FileAttributeParsingEvent fileEvent = (FileAttributeParsingEvent)event;
                return createIngestableFile(fileEvent);
            }
        }
        return null;
    }

    /** Implement the concrete detection of files to archive. */
    protected abstract boolean isIngestableNode(ParsingEvent event);
    /**  Implement the concrete way of archiving the files. */
    protected abstract IngestableFile createIngestableFile(FileAttributeParsingEvent fileEvent);
}

