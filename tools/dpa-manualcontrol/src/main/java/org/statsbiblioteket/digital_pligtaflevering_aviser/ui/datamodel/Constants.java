package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event;

public class Constants {
    public static final String STOPPED_EVENT = Event.STOPPED_STATE;
    public static final String APPROVED_EVENT = Event.APPROVED_STATE;
    public static final String MANUAL_QA_COMPLETE_EVENT = "ManualQA_Complete";
    public static final String EVENT_DELETED_EVENT = "Event_deleted_manually";
    public static final String VERA_PDF_ANALYZED_EVENT = "VeraPDF_Analyzed";
    public static final String WEEKDAYS_ANALYZED_EVENT = "Newspaper_Weekdays_Analyzed";
    public static final String STATISTICS_GENERATED_EVENT = "Statistics_generated";
    public static final String DS_VERAPDFREPORT = "VERAPDFREPORT";
    public static final String DS_NEWSPAPERWEEKDAY = "NEWSPAPERWEEKDAY";
    public static final String DS_DELIVERYSTATISTICS = "DELIVERYSTATISTICS";
    public static final String AGENT_IDENTIFIER_VALUE = "manualcontrol";
    public static final String OVERRIDE_EVENT = "Override_event";
}
