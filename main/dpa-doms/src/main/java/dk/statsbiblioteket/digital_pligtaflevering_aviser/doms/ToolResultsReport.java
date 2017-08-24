package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import com.google.common.base.Throwables;
import javaslang.control.Either;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A ToolResultsReport converts a list of <tt>Try&lt;A></tt></pre> into a String.  The successful ones are rendered by
 * the renderResultFunction, and the failed (which threw an exception) ones have their stack traces added.   This is
 * useful for creating event descriptions from multiple tasks.
 */
public class ToolResultsReport implements BiFunction<DomsItem, List<Either<ToolThrewException, ToolResult>>, ToolResult> {
    private final BiFunction<List<ToolResult>, List<ToolResult>, String> renderResultFunction;
    private final Function<Throwable, String> stackTraceRenderer;
    private final Consumer<ToolThrewException> stackTraceLogger;

    /**
     * This is the method intended to be used in actual code
     */
    @Inject
    public ToolResultsReport(BiFunction<List<ToolResult>, List<ToolResult>, String> renderResultFunction, Consumer<ToolThrewException> stackTraceLogger) {
        this(renderResultFunction, Throwables::getStackTraceAsString, stackTraceLogger);
    }

    /**
     * This is to be used in unit tests, to control stack traces so they can be compared as strings
     */

    public ToolResultsReport(BiFunction<List<ToolResult>, List<ToolResult>, String> renderResultFunction, Function<Throwable, String> stackTraceRenderer,
                             Consumer<ToolThrewException> stackTraceLogger) {
        this.renderResultFunction = renderResultFunction;
        this.stackTraceRenderer = stackTraceRenderer;
        this.stackTraceLogger = stackTraceLogger;
    }

    @Override
    public ToolResult apply(DomsItem resultItem, List<Either<ToolThrewException, ToolResult>> eitherList) {

        // find all that threw exception.

        List<ToolThrewException> threwException = eitherList.stream()
                .filter(Either::isLeft)
                .map(Either::getLeft)
                .collect(Collectors.toList());

        // find all that completed successfully

        List<ToolResult> ok = eitherList.stream()
                .filter(Either::isRight)
                .map(Either::get)
                .filter(ToolResult::isSuccess)
                .collect(Collectors.toList());

        List<ToolResult> failed = eitherList.stream()
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

        // a, b, c:
        // ----
        // (root cause stack trace)

        String renderedIdsForRootCauses = idsForRootCauseMap.entrySet().stream()
                .map(entry -> String.join(", ", entry.getValue()) + ":\n---\n" + entry.getKey() + "\n")
                .collect(Collectors.joining("\n"));

        String renderedStacktraces = threwException.stream()
                .sorted(Comparator.comparing(t -> t.id()))
                .peek(stackTraceLogger)
                .map(t -> t.id() + ":\n" + stackTraceRenderer.apply(t.getException()) + "\n")
                .collect(Collectors.joining("\n"));

        if (failed.size() == 0 && threwException.size() == 0) { // Nothing went wrong.
            return new ToolResult(resultItem, true, renderResultFunction.apply(ok, failed));
        } else {
            return new ToolResult(resultItem, false,
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
    @Deprecated
    public static Either<ToolThrewException, ToolResult> applyOn(DomsItem item, Function<DomsItem, ToolResult> mapping) {
        try {
            return Either.right(mapping.apply(item));
        } catch (Exception e) {
            return Either.left(new ToolThrewException(item, e));
        }
    }

    public final static BiFunction<List<ToolResult>, List<ToolResult>, String> OK_COUNT_FAIL_LIST_RENDERER =
            (ok, failed) -> ok.size() + " ok" +
                    (failed.size() > 0
                            ? "\n\n" + failed.size() + " failed:\n---\n" +
                            failed.stream()
                                    .map(t -> t.id() + ": " + t.getHumanlyReadableMessage())
                                    .collect(Collectors.joining("\n"))
                            : ""
                    );

}
