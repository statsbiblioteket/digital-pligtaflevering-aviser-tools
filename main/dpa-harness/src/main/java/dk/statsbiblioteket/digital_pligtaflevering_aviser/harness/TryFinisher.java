package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import javaslang.control.Try;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  A TryFinisher converts a list of <tt>Try&lt;A></tt></pre> into a String.  The successful
 *  ones are rendered by the renderResultFunction, and the failed (which threw an exception) ones
 *  have their stack traces added.   This is useful for creating event descriptions from
 *  multiple tasks.
 */
public class TryFinisher<A> implements Function<List<Try<A>>, String> {
    private final Function<Stream<A>, String> renderResultFunction;
    private final Function<Throwable, String> stackTraceRenderer;

    /**
     * This is the method intended to be used in actual code
     */
    @Inject
    public TryFinisher(Function<Stream<A>, String> renderResultFunction) {
        this(renderResultFunction, throwable-> {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        });
    }

    /**
     * This is to be used in unit tests, to control stack traces so they can be compared as strings
     */

    public TryFinisher(Function<Stream<A>, String> renderResultFunction, Function<Throwable, String> stackTraceRenderer) {
        this.renderResultFunction = renderResultFunction;
        this.stackTraceRenderer = stackTraceRenderer;
    }

    @Override
    public String apply(List<Try<A>> tries) {

        // Render those that did not throw an exception.

        String renderedNonFails = renderResultFunction.apply(tries.stream().filter(Try::isSuccess).map(Try::get));

        // Add stacktraces for those that failed.

        final String renderedStackTraces = tries.stream()
                .filter(Try::isFailure)
                .map(Try::getCause)
                .map(stackTraceRenderer)
                .collect(Collectors.joining("\n\n"));

        if (renderedStackTraces.length() > 0) {
            return renderedNonFails + "\n\n" + renderedStackTraces;
        } else {
            return renderedNonFails;
        }
    }
}
