package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import java.util.Optional;

/**
 * <p>TaskResult holds the result of invoking a task - a flag to indicate if the task considered itself to be successful
 * or not, and a humanly readable result which can be used to create a report.  In case of failure an exception may be
 * provided for a stack trace.   The task invoker can then decide what to do based on this information. </p> <p>This is
 * inspired by the ResultCollector in the newspaper-batch-event-framework</p>
 */
public class TaskResult {
    private final Optional<Throwable> throwable;

    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public String getResult() {
        return result;
    }

    private String result;

    public TaskResult(boolean success, String result) {
        this.success = success;
        this.result = result;
        this.throwable = Optional.empty();
    }

    @Override
    public String toString() {
        return "TaskResult{" +
                "throwable=" + throwable +
                ", success=" + success +
                ", result='" + result + '\'' +
                '}';
    }

    public TaskResult(boolean success, String result, Throwable throwable) {
        this.success = success;
        this.result = result;
        this.throwable = Optional.of(throwable);
//        this.success = success;
//        StringWriter sw = new StringWriter();
//        PrintWriter pw = new PrintWriter(sw);
//        t.printStackTrace(pw);
//        this.result = pw.toString();
    }
}
