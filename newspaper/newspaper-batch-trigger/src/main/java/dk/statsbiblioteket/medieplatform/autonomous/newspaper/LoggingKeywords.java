package dk.statsbiblioteket.medieplatform.autonomous.newspaper;

/**
 * Keywords of logs which is written to the logfile and used by logstash indicate progress in Kibana
 */
public class LoggingKeywords {


    public static final String CREATE_DELIVERY_START_LOGTEXT = "EnteredMainForDelivery dl_{}_rt{} ";
    public static final String CREATE_DELIVERY_FINISH_LOGTEXT = "FinishedMainForDelivery dl_{}_rt{} Took: {} ms ";

    }
