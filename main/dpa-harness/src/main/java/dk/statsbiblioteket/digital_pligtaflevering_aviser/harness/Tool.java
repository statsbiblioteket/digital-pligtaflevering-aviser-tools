package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import java.util.concurrent.Callable;

/**
 * A AutonomousPreservzationTool is responsible for executing something in an orderly fashion even if
 * coce executed throws exceptions.  The string returned should be suitable for logging.
 */
public interface Tool extends Callable<String> {
}
