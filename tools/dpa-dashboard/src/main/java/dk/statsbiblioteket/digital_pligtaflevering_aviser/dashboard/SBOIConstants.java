package dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard;

/**
 * Summa query strings to ensure a central location. They are being prefixed
 * later with the correct collection hence they have to start with AND)
 *
 */
public class SBOIConstants {

    /**
     * query for those where roundtrip data has been received successfully but
     * not archived or put under manual control)
     *
     */
    public static final String Q_READY_TO_INGEST
            = " AND  +success_event:\"Data_Received\"  AND  ( ( +old_event:\"\" ) OR ( -event:\"\" ) )  "
            + "AND  -event:\"Manually_stopped\"  AND  -event:\"Data_Archived\"  AND  +item_model:\"doms:ContentModel_DPARoundTrip\"";
    /**
     * query for those where the ingest has happened but failed, and is not put
     * under manual control
     *
     */
    public static final String Q_INGEST_FAILED = " and +event:\"Data_Archived\" and -success_event:\"Data_Archived\" and -event:\"Manually_stopped\"";
    
    public static final String Q_INGEST_SUCCESSFUL = "+item_model:\"doms:ContentModel_DPARoundTrip\" AND +success_event:Data_Archived";
    /**
     * query for those which has been put under manual_control.  This effectively stops every downstream processing.
     * 
     */
    public static final String Q_MANUAL_CONTROL = " and +event:\"Manually_stopped\"";
    
}
