package org.test;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard.RepositoryProvider;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;

import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class FetchEventStructure {

    ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties("/backend.properties");

    DomsRepository repository = new RepositoryProvider().apply(map);

    DomsModule domsModule = new DomsModule();

    public FetchEventStructure() {

    }

    public Stream<DomsItem> getState(String state) {

        switch(state) {
            case "Manually_stopped":
                return getStateManuallyStopped();
            case "Data_Created":
                return getStateCreated();
            case "Data_Archived":
                return getStateIngested();
        }
        return null;
    }


    public Stream<DomsItem> getStateManuallyStopped() {
        return repository.query(domsModule.providesQuerySpecification(
                "Manually_stopped", "", "", "doms:ContentModel_RoundTrip"));
    }

    public Stream<DomsItem> getStateCreated() {
        return repository.query(domsModule.providesQuerySpecification(
                "Data_Created", "Data_Archived,Manually_stopped", "", "doms:ContentModel_RoundTrip"));
    }

    public Stream<DomsItem> getStateIngested() {
        return repository.query(domsModule.providesQuerySpecification(
                "Data_Archived", "Manually_stopped", "", "doms:ContentModel_RoundTrip")
        );
    }

    public void setEvent(String id, String eventName, String outcomeParameter, String message) {


        boolean outcome = outcomeParameter == null ? true : Boolean.parseBoolean(outcomeParameter);

        DomsItem item = repository.lookup(new DomsId(id));

        item.appendEvent("dashboard", new java.util.Date(), message == null ? "" : message, eventName, outcome);
    }

    public Stream<DomsItem> getStopped() {
        return repository.query(domsModule.providesQuerySpecification(
                "Manually_stopped", "", "", "doms:ContentModel_RoundTrip"));
    }


}
