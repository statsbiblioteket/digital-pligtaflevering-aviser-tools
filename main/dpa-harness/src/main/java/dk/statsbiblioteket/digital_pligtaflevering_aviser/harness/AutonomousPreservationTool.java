package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

/**
 * A AutonomousPreservzationTool is responsible for executing something in an orderly fashion even if
 * coce executed throws exceptions.
 */
public interface AutonomousPreservationTool {
    void execute();
}
