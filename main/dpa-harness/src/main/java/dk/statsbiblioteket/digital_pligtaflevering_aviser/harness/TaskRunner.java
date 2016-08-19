package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.RepositoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class TaskRunner<I extends RepositoryItem> implements Runnable {

    protected Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private TaskComponent<I, String> taskComponent;

    public TaskRunner(TaskComponent<I, String> taskComponent) {
        this.taskComponent = taskComponent;
    }

    @Override
    public void run() {

        long startTime = System.currentTimeMillis();
        log.info("*** Started at " + new Date());
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> log.info("*** Stopped at {} - {} ms.", new Date(), System.currentTimeMillis() - startTime)
        ));


        int exitCode = 0;
        try {
            Stream<Stream<I>> s1 = taskComponent.provideItems();
            Stream<String> s3 = s1.flatMap(s -> s).map(taskComponent.provideTask());
            // what to do with the result?
            String s4 = s3.collect(Collectors.joining(", "));
            // for now just log it...
            log.info("{}", s4);
        } catch (Throwable e) {
            log.error("processing failed, halting", e);
            // ? e.printStackTrace(System.err);
            exitCode = -1;
        }
        // FIXME: ensure log file is properly flushed by backend
        System.exit(exitCode);
    }
}
