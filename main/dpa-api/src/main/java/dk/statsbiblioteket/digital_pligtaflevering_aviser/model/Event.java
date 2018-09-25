package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

/**
 * An Event represents #FIXME# something that has happened at a given time,
 * related to a given RepositoryItem in a Repository.
 */
public interface Event {
    String STOPPED_STATE = "Manually_stopped";
    String APPROVED_STATE = "Roundtrip_approved";
    String DATA_RECEIVED = "Data_Received";
    String MUTATION_RECEIVED = "Mutation_Received";
}
