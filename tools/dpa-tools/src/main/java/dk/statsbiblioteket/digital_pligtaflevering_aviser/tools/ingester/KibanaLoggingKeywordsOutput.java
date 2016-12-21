package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester;

/**
 * Keywords of logs which is written to the logfile and used by logstash indicate progress in Kibana
 */
public class KibanaLoggingKeywordsOutput {
    public static final String START_PDF_FILE_INGEST = "EnteredPdfFileIngest {} ";
    public static final String FINISHED_PDF_FILE_INGEST = "FinishedPdfFileIngest {} Took: {} ms ";

    public static final String START_BATCH_INGEST = "EnteredBatchIngest {} ";
    public static final String FINISHED_BATCH_INGEST = "FinishedBatchIngest {} Took: {} ms ";
}