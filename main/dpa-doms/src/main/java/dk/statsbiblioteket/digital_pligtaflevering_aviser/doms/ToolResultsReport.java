package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import com.google.common.base.Throwables;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.streams.IdValue;
import javaslang.control.Either;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * A ToolResultsReport converts a list of <tt>Try&lt;A></tt></pre> into a String.  The successful ones are rendered by
 * the renderResultFunction, and the failed (which threw an exception) ones have their stack traces added.   This is
 * useful for creating event descriptions from multiple tasks.
 *
 * @noinspection ALL
 */
public class ToolResultsReport<K> implements BiFunction<K, List<IdValue<K, Either<Exception, ToolResult>>>, ToolResult> {
    private final BiFunction<List<IdValue<K, ToolResult>>, List<IdValue<K, ToolResult>>, String> renderResultFunction;
    private final Function<Throwable, String> stackTraceRenderer;
    private final BiConsumer<K, Exception> stackTraceLogger;

    /**
     * <p>This is the method intended to be used in actual code.</p>
     * <p><code>stackTraceLogger</code> allows for the calling code to determine how a given exception should be logged.
     *  An example is <code> (id, t) -> log.error("id: {}", id, t)</code>.  Note that the logger is in the scope of the
     * caller, so it is the logger variable defined _there_ and the class name is that of the caller, not deep in some
     * library!</p>
     */
//    @Inject
//    public ToolResultsReport(BiFunction<List<IdValue<DomsItem, ToolResult>>, List<IdValue<DomsItem, ToolResult>>, String> renderResultFunction, BiConsumer<DomsItem, Exception> stackTraceLogger) {
//        this(renderResultFunction, Throwables::getStackTraceAsString, stackTraceLogger);
//    }

    /**
     * This is to be used in unit tests, to control stack traces so they can be compared as strings
     */

    @Inject
    public ToolResultsReport(BiFunction<List<IdValue<K, ToolResult>>, List<IdValue<K, ToolResult>>, String> renderResultFunction,
                             BiConsumer<K, Exception> stackTraceLogger,
                             Function<Throwable, String> stackTraceRenderer
    ) {
        this.renderResultFunction = renderResultFunction;
        this.stackTraceLogger = stackTraceLogger;
        this.stackTraceRenderer = stackTraceRenderer;
    }

    @Override
    public ToolResult apply(K domsItem, List<IdValue<K, Either<Exception, ToolResult>>> idValues) {

        // These can probably be written smarter, but for now keep it simple.

        // find all that threw exception.

        List<IdValue<K, Exception>> threwException = idValues.stream()
                .filter(c -> c.filter(Either::isLeft))
                .map(c -> c.map(Either::getLeft))
                .collect(toList());

        // find all that completed successfully

        List<IdValue<K, ToolResult>> ok = idValues.stream()
                .filter(c -> c.filter(Either::isRight))
                .map(c -> c.map(Either::get))
                .filter(c -> c.filter(ToolResult::isSuccess))
                .collect(toList());

        List<IdValue<K, ToolResult>> failed = idValues.stream()
                .filter(c -> c.filter(Either::isRight))
                .map(c -> c.map(Either::get))
                .filter(c -> c.filter(t -> t.isSuccess() == false))
                .collect(toList());

        // We are interested in finding common root causes (innermost exception) and list their ids.
        // Hence construct the string representation of the root cause stack trace and use that as the common
        // key to collect all id's with that inner root cause.

        Map<String, List<String>> idsForRootCauseMap = threwException.stream()
                .collect(
                        groupingBy(c -> stackTraceRenderer.apply(Throwables.getRootCause(c.value())),
                                TreeMap::new,  // sorted keys
                                mapping(c -> c.id().toString(), toList()))
                );

        // a, b, c:
        // ----
        // (root cause stack trace)

        String renderedIdsForRootCauses = idsForRootCauseMap.entrySet().stream()
                .map(entry -> String.join(", ", entry.getValue()) + ":\n---\n" + entry.getKey() + "\n")
                .collect(joining("\n"));

        String renderedStacktraces = threwException.stream()
                .sorted(Comparator.comparing(c -> c.id().toString())) // sort by string representation of id.
                .peek(c -> stackTraceLogger.accept(c.id(), c.value()))
                .map(c -> c.id() + ":\n" + stackTraceRenderer.apply(c.value()) + "\n")
                .collect(joining("\n"));

        if (failed.size() == 0 && threwException.size() == 0) { // Nothing went wrong.
            return new ToolResult(true, renderResultFunction.apply(ok, failed));
        } else {
            return new ToolResult(false,
                    renderResultFunction.apply(ok, failed)
                            //renderedOk
                            //+ "\n\nfailed:\n---\n" + renderedFails
                            + (threwException.size() > 0 ? "\n\n" + renderedIdsForRootCauses + "\n\n---\n" + renderedStacktraces : "")
            );
        }
    }

    /**
     * Method to invoke a mapping on an Id, and according to convention return an Either.Left in case of problems
     * capturing the exception and the id, or an Either.Right capturing the result.  We need this because we need to
     * store the id along with the exception for later.
     *
     * @param item the Id to pass in that we are working on.  Captured in the failure.
     * @return
     */
    public static <K> Either<Exception, ToolResult> applyOn(K item, Function<K, ToolResult> mapping) {
        try {
            return Either.right(mapping.apply(item));
        } catch (Exception e) {
            return Either.left(e);
        }
    }

    public static class OK_COUNT_FAIL_LIST_RENDERER<K> implements BiFunction<List<IdValue<K, ToolResult>>, List<IdValue<K, ToolResult>>, String> {
        @Override
        public String apply(List<IdValue<K, ToolResult>> ok, List<IdValue<K, ToolResult>> failed) {
            return ok.size() + " ok" +
                    (failed.size() > 0
                            ? "\n\n" + failed.size() + " failed:\n---\n" +
                            failed.stream()
                                    .map(c -> c.map((id, v) -> id + ": " + v.getHumanlyReadableMessage()).value())
                                    .collect(joining("\n"))
                            : ""
                    );
        }
    }
}
