package dk.statsbiblioteket.newspaper.bitrepository.ingester;

import java.net.MalformedURLException;
import java.net.URL;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.AbstractImageLocator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngestableFile;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;
import dk.statsbiblioteket.newspaper.bitrepository.ingester.utils.BatchMD5Validation;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;

/**
 * Concrete implementation of <code>AbstractImageLocator</code> defining how to find the jp2 files to store for
 * a newspaper batch.
 */
public class BatchImageLocator extends AbstractImageLocator {
    private final String batchDirUrl;
    private BatchMD5Validation md5Validation;

    public BatchImageLocator(TreeIterator treeIterator, String batchDirUrl, String batchFolder) {
        super(treeIterator);
        this.batchDirUrl = batchDirUrl;
        this.md5Validation = new BatchMD5Validation(this.batchDirUrl);
        this.md5Validation.readChecksums(batchFolder);
    }

    protected boolean isIngestableNode(ParsingEvent event) {
        return event.getName().endsWith(".pdf/contents");
    }

    @Override
    protected IngestableFile createIngestableFile(FileAttributeParsingEvent fileEvent) {
        return new IngestableFile(
                getFileID(fileEvent), getFileUrl(fileEvent), getChecksum(this.md5Validation.getExpectedMD5(getFileName(fileEvent))), null,
                "path:" + getFileName(fileEvent));
    }

    /**
     * Creates a fileID by supstitution the '/' path separators in the filename with '_'.
     */
    private String getFileID(FileAttributeParsingEvent event) {
        return NewspaperFileNameTranslater.getFileID(getFileName(event));
    }

    /**
     * Creates a url by prefixing the filename from the event with the <code>batchDirUrl</code> defined in
     * the properties.
     */
    private URL getFileUrl(FileAttributeParsingEvent event) {
        try {
            return new URL("file:///" + batchDirUrl + "/" + getFileName(event));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to create ingest url based on string: " + batchDirUrl + "/" + event.getName());
        }
    }

    private ChecksumDataForFileTYPE getChecksum(String checksum) {
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumValue(Base16Utils.encodeBase16(checksum));
        checksumData.setCalculationTimestamp(CalendarUtils.getNow());
        ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.MD5);
        checksumData.setChecksumSpec(checksumSpec);
        return checksumData;
    }

    private String getFileName(FileAttributeParsingEvent event ) {
        return event.getName().substring(0, event.getName().indexOf("/contents"));
    }
}

