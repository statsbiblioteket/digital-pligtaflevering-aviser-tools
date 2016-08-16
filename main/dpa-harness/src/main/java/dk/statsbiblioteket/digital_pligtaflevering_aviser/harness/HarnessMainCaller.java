package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

/**
 *
 */
public class HarnessMainCaller implements Runnable {
    private MainFunction mainFunction;
    private String[] args;

    public HarnessMainCaller(MainFunction mainFunction, String[] args) {
        this.mainFunction = mainFunction;
        this.args = args;
    }

    @Override
    public void run() {
        // log start
        Integer result = -1;
        try {
            result = mainFunction.apply(args);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            // log.error(e stuff)
        }
        // log stop, ensure log file is properly written by backend

        System.exit(result);
    }
}
