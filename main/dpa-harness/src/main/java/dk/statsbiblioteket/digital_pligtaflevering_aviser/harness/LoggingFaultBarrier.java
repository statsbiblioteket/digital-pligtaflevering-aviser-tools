package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

/**
 * <p>Wrap invoking run() on the given Runnable which is expected to catch all recoverable exceptions so
 * those actually thrown are fatal, in
 * <ol><li>a start log statement, and a at-shutdown log statment with a time-used-in-milliseconds</li>
 * <li>try-catch catching everything and logging the throwable caught and shut down using System.exit(-1)</li>
 * <li>If run without exceptions to completion System.exit(0) is invoked.</ol>
 * </ol>
 *
 * </p>
 *
 * <p>Note:  Never returns</p>
 */
public class LoggingFaultBarrier implements Runnable {

    static LocalDateTime startTime = LocalDateTime.now(); // initialized at class load time

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected final Runnable runnable;

    public LoggingFaultBarrier(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        log.info("*** Started at {}", startTime);

        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> log.info("*** Stopped at {} - {} ms.",
                        LocalDateTime.now(),
                        ChronoUnit.MILLIS.between(startTime, LocalDateTime.now())
                )));

        int exitCode = 0;
        try {
            runnable.run();
        } catch (Throwable e) {
            // It is open for discussion what exactly should happen here.  Log at error
            // level for now so logger backend can do stuff.
            log.error("Exception thrown, shutting down", e);
            exitCode = -1;
        }
        System.exit(exitCode);
    }

    public static void main(String[] args) {
        Logger mainLogger = LoggerFactory.getLogger(LoggingFaultBarrier.class);
        new LoggingFaultBarrier(
                () -> Stream.of("1", "2", "3").forEach(s -> mainLogger.info("{}", s))
        ).run();
    }
}
