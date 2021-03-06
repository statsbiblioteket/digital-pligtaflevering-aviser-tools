package dk.statsbiblioteket.newspaper.promptdomsingester.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.TreeProcessorAbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.promptdomsingester.IngesterInterface;
import dk.statsbiblioteket.newspaper.promptdomsingester.SimpleFedoraIngester;
import dk.statsbiblioteket.newspaper.promptdomsingester.util.ArticleTransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.promptdomsingester.util.BatchMD5SUMSValidation;
import dk.statsbiblioteket.util.Strings;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * The runnable component for the PromptDomsIngester
 */
public class RunnablePromptDomsIngester extends TreeProcessorAbstractRunnableComponent {
    private final EnhancedFedora eFedora;
    private static final String eventId = "Metadata_Archived";

    public RunnablePromptDomsIngester(Properties properties, EnhancedFedora eFedora) {
        super(properties);
        this.eFedora = eFedora;
    }

    @Override
    public String getEventID() {
        return eventId;
    }

    @Override
    public void doWorkOnItem(Batch batch, ResultCollector resultCollector) {
        try {
            BatchMD5SUMSValidation md5Validator = new BatchMD5SUMSValidation(getProperties().getProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER), getProperties().getProperty(ConfigConstants.ITERATOR_FILESYSTEM_IGNOREDFILES));
            if (!md5Validator.validation(batch.getFullID())) {
                List<String> validationFailureResult = md5Validator.getValidationResult();
                for (String failure : validationFailureResult) {
                    resultCollector.addFailure(batch.getFullID(), "md5Validator", this.getClass().getSimpleName(), "md5Validator validation was not accepted with message:" + failure);
                }
            } else {
                IngesterInterface ingester = SimpleFedoraIngester.getNewspaperInstance(
                        eFedora, new String[]{getProperties().getProperty(
                                ConfigConstants.DOMS_COLLECTION, "doms:DPA_Collection")});
                ingester.ingest(createIterator(batch));
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
