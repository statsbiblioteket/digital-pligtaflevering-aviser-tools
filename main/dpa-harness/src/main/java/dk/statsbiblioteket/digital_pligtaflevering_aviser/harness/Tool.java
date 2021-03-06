package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * <p>
 * A AutonomousPreservzationTool is responsible for executing something in an orderly fashion even if coce executed
 * throws exceptions.  The string returned should be suitable for logging. </p> <p>If <code>getMXBean</code></p>
 */
public interface Tool<E> extends Callable<List<E>> {
    public static final String AUTONOMOUS_THIS_EVENT = "autonomous.thisEvent";
}
