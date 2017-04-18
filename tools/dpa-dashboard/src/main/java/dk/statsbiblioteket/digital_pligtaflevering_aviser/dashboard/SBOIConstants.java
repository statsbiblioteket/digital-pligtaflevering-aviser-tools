package dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard;

public class SBOIConstants {
    public static final String Q_READY_TO_INGEST = " AND  +success_event:\"Data_Received\"  AND  ( ( +old_event:\"\" ) OR ( -event:\"\" ) )  AND  -event:\"Manually_stopped\"  AND  -event:\"Data_Archived\"  AND  +item_model:\"doms:ContentModel_DPARoundTrip\"";
}
