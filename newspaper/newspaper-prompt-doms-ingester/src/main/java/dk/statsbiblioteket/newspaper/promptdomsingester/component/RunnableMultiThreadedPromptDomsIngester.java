package dk.statsbiblioteket.newspaper.promptdomsingester.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.TreeProcessorAbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.newspaper.promptdomsingester.MultiThreadedFedoraIngester;
import dk.statsbiblioteket.util.Strings;

import java.util.Properties;

/**
 * This is the multithreaded doms ingester
 */
public class RunnableMultiThreadedPromptDomsIngester extends TreeProcessorAbstractRunnableComponent {

    private final EnhancedFedora eFedora;

    /**
     * Constructur
     *
     * @param properties
     * @param eFedora
     */
    public RunnableMultiThreadedPromptDomsIngester(Properties properties, EnhancedFedora eFedora) {
        super(properties);
        this.eFedora = eFedora;
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException | NullPointerException e) {
            return 4;
        }
    }

    @Override
    public String getEventID() {
        return "Metadata_Archived";
    }

    @Override
    public void doWorkOnItem(Batch batch, ResultCollector resultCollector) {
        TreeIterator iterator = createIterator(batch);
        MultiThreadedFedoraIngester ingester = new MultiThreadedFedoraIngester(
                eFedora,
                new String[]{getProperties().getProperty(ConfigConstants.DOMS_COLLECTION, "doms:Newspaper_Collection")},
                parseInt(getProperties().getProperty(ConfigConstants.THREADS_PER_BATCH)));
        try {
            ingester.ingest(iterator);
        } catch (Exception e) {
            resultCollector.addFailure(
                    batch.getFullID(),
                    "exception",
                    e.getClass().getSimpleName(),
                    "Exception during ingest: " + e.toString(),
                    Strings.getStackTrace(e));
        }
    }
}