package dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository;

/**
 * Defines the functionality for finding the files to ingest.
 *
 *
 */
public interface IngestableFileLocator {
    /**
     * Will return the next file to ingest .
     * @return A <code>IngestableFile</code> object representing the file to ingest, including the relevante
     * ingest attributes for the file.
     */
    IngestableFile nextFile();
}
