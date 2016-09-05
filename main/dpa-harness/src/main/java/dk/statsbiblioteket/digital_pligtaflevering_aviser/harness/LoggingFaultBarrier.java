package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.time.LocalDateTime.now;

/**
 * <p>Wrap invoking run() on the given Runnable which is expected to catch all recoverable exceptions so
 * those actually thrown are fatal, in
 * <ol><li>a start log statement, and a at-shutdown log statment with a time-used-in-milliseconds</li>
 * <li>try-catch catching everything and logging the throwable caught and shut down using System.exit(-1)</li>
 * <li>If run without exceptions to completion System.exit(0) is invoked.</ol>
 * </ol>
 * <p>Optionally can dump Heap (when run in a Hotspot JVM) to a file named by a lambda expression based on dump time.
 * </p>
 * <p>
 * <p>Note:  Never returns</p>
 */
public class LoggingFaultBarrier implements Runnable { // Implements Tool?

    public static final String JVM_DUMPHEAP = "jvm.dumpheap";
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected final Runnable runnable;
    private boolean dumpHeapOption;

    @Inject
    public LoggingFaultBarrier(Runnable runnable, @Named(JVM_DUMPHEAP) boolean dumpHeapOption) {
        this.runnable = runnable;
        this.dumpHeapOption = dumpHeapOption;
    }

    @Override
    public void run() {

        log.info("*** Started at {} - {} ms since JVM start.", now(), getRuntimeMXBean().getUptime());

        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> log.info("*** Stopped at {} - {} ms since JVM start.", now(), getRuntimeMXBean().getUptime()
                )));

        int exitCode = 0;
        try {
            runnable.run();
        } catch (Throwable e) {
            // It is open for discussion what exactly should happen here.  Log at error
            // level for now so logger backend can do stuff.
            log.error("Runnable threw exception, shutting down:", e);
            exitCode = -1;
        }
        // logger framework must be configured to shut down properly when jvm exits.

        if (dumpHeapOption) {
            JavaVirtualMachineHelper.dumpHeap(now -> now + ".hprof");
        }
        System.exit(exitCode);
    }
}
