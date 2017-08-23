package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import com.google.common.base.Throwables;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Id;
import javaslang.control.Either;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A ToolResultsReport converts a list of <tt>Try&lt;A></tt></pre> into a String.  The successful ones are rendered by
 * the renderResultFunction, and the failed (which threw an exception) ones have their stack traces added.   This is
 * useful for creating event descriptions from multiple tasks.
 */
public class ToolResultsReport<A extends ToolCompletedResult> implements BiFunction<Id, List<Either<ToolThrewExceptionResult, A>>, ToolCompletedResult> {
    private final Function<Stream<Either<A, ToolThrewExceptionResult>>, String> renderResultFunction;
    private final Function<Throwable, String> stackTraceRenderer;

    /**
     * This is the method intended to be used in actual code
     */
    @Inject
    public ToolResultsReport(Function<Stream<Either<A, ToolThrewExceptionResult>>, String> renderResultFunction) {
        this(renderResultFunction, Throwables::getStackTraceAsString);
    }

    /**
     * This is to be used in unit tests, to control stack traces so they can be compared as strings
     */

    public ToolResultsReport(Function<Stream<Either<A, ToolThrewExceptionResult>>, String> renderResultFunction, Function<Throwable, String> stackTraceRenderer) {
        this.renderResultFunction = renderResultFunction;
        this.stackTraceRenderer = stackTraceRenderer;
    }

    @Override
    public ToolCompletedResult apply(Id id, List<Either<ToolThrewExceptionResult, A>> eitherList) {

        // find all that threw exception.

        List<ToolThrewExceptionResult> threwException = eitherList.stream()
                .filter(Either::isLeft)
                .map(Either::getLeft)
                .collect(Collectors.toList());

        // find all that completed successfully

        List<A> ok = eitherList.stream()
                .filter(Either::isRight)
                .map(Either::get)
                .filter(ToolCompletedResult::isSuccess)
                .collect(Collectors.toList());

        List<A> failed = eitherList.stream()
                .filter(Either::isRight)
                .map(Either::get)
                .filter(a -> !a.isSuccess())
                .collect(Collectors.toList());

        // We are interested in finding common root causes (innermost exception) and list their ids.
        // Hence construct the string representation of the root cause stack trace and use that as the common
        // key to collect all id's with that inner root cause.

        Map<String, List<String>> idsForRootCauseMap = threwException.stream()
                .collect(Collectors.groupingBy(t -> stackTraceRenderer.apply((Throwables.getRootCause(t.getException()))),
                        Collectors.mapping(t -> t.id(), Collectors.toList())));

        // We are interested in the number of those successful, the exact id's that were not succesful, and the stacktraces of
        // those that threw an exception.

        String renderedOk = ok.size() + " ok";
        String renderedFails = failed.stream()
                .map(t -> t.id() + ": " + t.getPayload())
                .collect(Collectors.joining("\n"));

        // a, b, c:
        // ----
        // (root cause stack trace)

        String renderedIdsForRootCauses = idsForRootCauseMap.entrySet().stream()
                .map(entry -> String.join(", ", entry.getValue()) + ":\n---\n" + entry.getKey() + "\n")
                .collect(Collectors.joining("\n"));

        String renderedStacktraces = threwException.stream()
                .map(t -> t.id() + ":\n" + stackTraceRenderer.apply(t.getException()) + "\n")
                .collect(Collectors.joining("\n"));

        if (failed.size() == 0 && threwException.size() == 0) { // Nothing went wrong.
            return new ToolCompletedResult(id, true, renderedOk);
        } else {
            return new ToolCompletedResult(id, false, renderedOk
                    + "\n\nfailed:\n---\n" + renderedFails
                    + (threwException.size() > 0 ? "\n\n" + renderedIdsForRootCauses + "\n\n---\n" + renderedStacktraces : "")
            );
        }
    }
}
