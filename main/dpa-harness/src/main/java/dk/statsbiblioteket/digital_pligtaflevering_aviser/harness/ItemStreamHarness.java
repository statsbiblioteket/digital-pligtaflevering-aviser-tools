package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/**
 * <ul>
 * <li>Given a stream of item results, execute it until we run out of items (then execute
 * System.exit(0)) or an exception
 * occurs which we log and execute System.exit(-1).
 * <li>
 * Log start and stop times providing runtime info and guaranteeing that logfiles are complete for a run.
 * </li>
 * <li>Each V in the steam is logged if not null and the string representation is different from the empty string. </li>
 * </ul>
 */
public class ItemStreamHarness<V> implements Runnable { // FIXME:  Rename me.

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected Stream<V> callableStream;

    public ItemStreamHarness(Stream<V> callableStream) {
        this.callableStream = callableStream;
    }

    @Override
    public void run() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("*** Started at {}", startTime);
        Runtime.getRuntime().addShutdownHook(new Thread( // execute at jvm shutdown time
                () -> log.info("*** Stopped at {} - {} ms.",
                        LocalDateTime.now(),
                        ChronoUnit.MILLIS.between(startTime, LocalDateTime.now())
                )));

        int exitCode = 0;
        try {
            callableStream.forEach(s -> log.info("{}", s));
        } catch (Throwable e) {
            log.error("Exception thrown when none expected, shutting down", e);
            // ? e.printStackTrace(System.err);
            exitCode = -1;
        }
        // FIXME: ensure log file is properly flushed by backend. http://jira.qos.ch/browse/SLF4J-192
        System.exit(exitCode);
    }
}
