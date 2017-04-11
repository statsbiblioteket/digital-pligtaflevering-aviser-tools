package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;


import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class FetchEventStructure {

    private DomsRepository repository;
    private DomsModule domsModule = new DomsModule();


    public FetchEventStructure(DomsRepository repository) {
        this.repository = repository;
    }

    public Stream<DomsItem> getDeliveryList(EventStatus eventStatus) {
        switch(eventStatus) {
            case READYFORMANUALCHECK:
                return getReadyForMaual();
            case DONEMANUALCHECK:
                return getDoneManual();
        }
        return null;
    }

    public DomsItem lookup(String id) {
        DomsItem item = repository.lookup(new DomsId(id));
        return item;
    }


    public Stream<DomsItem> getReadyForMaual() {
        return repository.query(domsModule.providesQuerySpecification(
                "Statistics_generated", "ManualValidationDone", "", "doms:ContentModel_DPARoundTrip")
        );
    }

    public Stream<DomsItem> getDoneManual() {
        return repository.query(domsModule.providesQuerySpecification(
                "Statistics_generated,ManualValidationDone", "", "", "doms:ContentModel_DPARoundTrip")
        );
    }

    public void setEvent(String id, String eventName, String outcomeParameter, String message) {

        boolean outcome = outcomeParameter == null ? true : Boolean.parseBoolean(outcomeParameter);
        DomsItem item = repository.lookup(new DomsId(id));
        item.appendEvent("dashboard", new java.util.Date(), message == null ? "" : message, eventName, outcome);
    }

    public enum EventStatus {
        READYFORMANUALCHECK, DONEMANUALCHECK;
    }
}
