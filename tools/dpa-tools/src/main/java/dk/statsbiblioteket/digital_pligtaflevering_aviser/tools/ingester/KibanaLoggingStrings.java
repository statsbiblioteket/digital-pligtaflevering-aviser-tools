package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester;

/**
 * Keywords of logs which is written to the logfile and used by logstash indicate progress in Kibana
 */
public class KibanaLoggingStrings {
    public static final String START_PDF_FILE_INGEST = "EnteredPdfFileIngest {} ";
    public static final String FINISHED_PDF_FILE_INGEST = "FinishedPdfFileIngest {} Took: {} ms ";

    public static final String START_DELIVERY_INGEST = "EnteredDeliveryIngest {} ";
    public static final String DELIVERY_CONTENT_INFO = "DeliveryContentInformation {} articles: {} pages: {} ";
    public static final String FINISHED_DELIVERY_INGEST = "FinishedDeliveryIngest {} Took: {} ms ";

    public static final String START_DELIVERY_XML_VALIDATION_AGAINST_XSD = "EnteredDeliveryXSDValidation {} ";
    public static final String FINISHED_DELIVERY_XML_VALIDATION_AGAINST_XSD = "FinishedDeliveryXSDValidation {} Took: {} ms ";

    public static final String FINISHED_DELIVERY_PDFINVOKE = "FinishedDeliveryPDFInvoke {} Took: {} ms ";
    public static final String FINISHED_FILE_PDFINVOKE = "FinishedFilePDFInvoke {} Took: {} ms ";

    public static final String FINISHED_DELIVERY_PDFANALYZE = "FinishedDeliveryPDFAnalyze {} Took: {} ms ";
    public static final String FINISHED_FILE_PDFANALYZE = "FinishedFilePDFAnalyze {} Took: {} ms ";

    public static final String START_GENERATE_STATISTICS = "EnteredGenerateStatistics {} ";
    public static final String FINISHED_GENERATE_STATISTICS = "FinishedGenerateStatistics {} Took: {} ms ";

}