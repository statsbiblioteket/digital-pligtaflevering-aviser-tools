package dk.statsbiblioteket.newspaper.promptdomsingester.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.*;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.promptdomsingester.IngesterInterface;
import dk.statsbiblioteket.newspaper.promptdomsingester.SimpleFedoraIngester;
import dk.statsbiblioteket.newspaper.promptdomsingester.util.ArticleTransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.promptdomsingester.util.BatchMD5Validation;
import dk.statsbiblioteket.util.Strings;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * The runnable component for the PromptDomsIngester
 */
public class RunnablePromptDomsIngester extends TreeProcessorAbstractRunnableComponent {
    private final EnhancedFedora eFedora;
    private BatchMD5Validation md5Validator;
    private final static String eventId = "Metadata_Archived";

    public RunnablePromptDomsIngester(Properties properties, EnhancedFedora eFedora) {
        super(properties);
        this.eFedora = eFedora;
        md5Validator = new BatchMD5Validation(getProperties().getProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER));
    }

    @Override
    public String getEventID() {
        return eventId;
    }

    @Override
    public void doWorkOnItem(Batch batch, ResultCollector resultCollector) {
        try {
            if(md5Validator.validation(batch.getFullID())) {
                IngesterInterface ingester = SimpleFedoraIngester.getNewspaperInstance(
                        eFedora, new String[]{getProperties().getProperty(
                                ConfigConstants.DOMS_COLLECTION, "doms:Newspaper_Collection")}
                );
                ingester.ingest(createIterator(batch));
            } else {
                resultCollector.setPreservable(false);
                resultCollector.addFailure("md5Validator", "md5Validator", "BatchMD5Validation", "md5Validator validation was not accepted");
            }
        } catch (Exception e) {
            resultCollector.addFailure(
                    batch.getFullID(),
                    "exception",
                    e.getClass().getSimpleName(),
                    "Exception during ingest: " + e.toString(),
                    Strings.getStackTrace(e));
        }
    }

    @Override
    protected TreeIterator createIterator(Batch batch) {
        String dataFilePattern = getProperties().getProperty(ConfigConstants.ITERATOR_DATAFILEPATTERN, ArticleTransformingIteratorForFileSystems.DATA_FILE_PATTERN_VALUE);
        boolean useFileSystem = Boolean.parseBoolean(
                getProperties().getProperty(ConfigConstants.ITERATOR_USE_FILESYSTEM, "true"));

        if (useFileSystem) {
            File scratchDir = new File(getProperties().getProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER));
            File batchDir = new File(scratchDir, batch.getFullID());
            String groupingChar = Pattern
                    .quote(getProperties().getProperty(ConfigConstants.ITERATOR_FILESYSTEM_GROUPINGCHAR, "."));

            String checksumPostFix = getProperties().getProperty(ConfigConstants.ITERATOR_FILESYSTEM_CHECKSUMPOSTFIX,
                    TransformingIteratorForFileSystems.CHECKSUM_POSTFIX_DEFAULT_VALUE);
            String[] ignoredFiles = getProperties().getProperty(ConfigConstants.ITERATOR_FILESYSTEM_IGNOREDFILES,
                    TransformingIteratorForFileSystems.IGNORED_FILES_DEFAULT_VALUE).split(",");
            for (int i = 0; i < ignoredFiles.length; i++) {
                ignoredFiles[i] = ignoredFiles[i].trim();
            }
            return new ArticleTransformingIteratorForFileSystems(batchDir, groupingChar, dataFilePattern, checksumPostFix, Arrays.asList(ignoredFiles));

        }
        return null;
    }
}
