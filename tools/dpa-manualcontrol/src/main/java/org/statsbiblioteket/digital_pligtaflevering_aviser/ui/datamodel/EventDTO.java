package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import dk.statsbiblioteket.medieplatform.autonomous.Event;

import java.util.Date;

public class EventDTO {

    private String eventID;
    private boolean success;
    private String details;
    private Date date;

    public EventDTO(String eventID, boolean success, String details, Date date) {
        this.eventID=eventID;
        this.success=success;
        this.details=details;
        this.date=date;
    }

    public String getEventID() {
        return this.eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + (this.date == null ? 0 : this.date.hashCode());
        result = 31 * result + (this.eventID == null ? 0 : this.eventID.hashCode());
        result = 31 * result + (this.success ? 1231 : 1237);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof Event)) {
            return false;
        } else {
            EventDTO other = (EventDTO)obj;
            if (this.date == null) {
                if (other.date != null) {
                    return false;
                }
            } else if (!this.date.equals(other.date)) {
                return false;
            }

            if (this.eventID == null) {
                if (other.eventID != null) {
                    return false;
                }
            } else if (!this.eventID.equals(other.eventID)) {
                return false;
            }

            return this.success == other.success;
        }
    }

    public String toString() {
        return "Event [eventID=" + this.eventID + ", success=" + this.success + ", details=" + this.details + ", date=" + this.date + "]";
    }
}
