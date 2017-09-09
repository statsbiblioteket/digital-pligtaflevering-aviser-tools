package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Id;
import javaslang.control.Either;
import org.junit.Test;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReport.applyOn;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * Check that the ToolResultsReport actually invokes the passed-in string converter to generate a report for the
 * non-failing Try-entries, and that the invoked StackTrace renderer method is actually invoked for each failing
 * Try-entry with a blank line between each and that the final report consist of these two concatenated.
 */
public class ToolResultsReportTest {

    protected DomsItem id0 = i("*");

    // just get message instead of printing full stacktrace.
    protected final Function<Throwable, String> stacktraceRendering = Throwable::getMessage;

    // Fail for id "A","B","C". Otherwise success is if the id.length is an odd length.
    protected final BiFunction<DomsItem, String, Either<ToolThrewException, ToolResult>> testTool =
            (DomsItem resultItem, String output) -> applyOn(resultItem, item -> {
                if (item.getDomsId().id().equals("A") || item.getDomsId().id().equals("B") || item.getDomsId().id().equals("C")) {
                    throw new IllegalArgumentException(item.getDomsId().id() + ": '" + output + "'");
                } else {
                    return new ToolResult(resultItem, (resultItem.getDomsId().id().length() % 2) == 1, output);
                }
            });

    // We are interested in the number of those successful, the exact id's that were not succesful, and the stacktraces of
    // those that threw an exception.


    // we do not want to actually log the captured exceptions
    protected Consumer<ToolThrewException> noLogging = throwable -> { };

    //
    protected final ToolResultsReport tf = new ToolResultsReport(ToolResultsReport.OK_COUNT_FAIL_LIST_RENDERER, stacktraceRendering, noLogging);

    protected Function<ToolResult, String> toString = tr -> tr.id() + " " + tr.isSuccess() + ": " + tr.getHumanlyReadableMessage();

    protected Function<List<Either<ToolThrewException, ToolResult>>, String> f = eitherList -> toString.apply(tf.apply(id0, eitherList));

    // -- the below code is hard to read as Java 8 does not support multiline strings.  Note that IntelliJ allows for
    // -- "Alt-Enter->Copy String concatenation text to clipboard" for string concatenations where it
    // -- can be inspected easier.

    @Test
    public void noItems() {
        assertEquals("* true: 0 ok", f.apply(emptyList()));
    }

    @Test
    public void singleSuccessful() {
        assertEquals("* true: 1 ok", f.apply(singletonList(testTool.apply(i("1"), "x"))));
    }

    @Test
    public void singleFailing() {

        assertEquals("* false: 0 ok\n\n1 failed:\n---\n22: ", f.apply(singletonList(testTool.apply(i("22"), ""))));
    }

    @Test
    public void oneFailingOneThrowing() {

        assertEquals("* false: 0 ok\n\n1 failed:\n---\n22: \n\nA:\n---\nA: 'Amsg'\n\n\n---\nA:\nA: 'Amsg'\n",
                f.apply(asList(testTool.apply(i("22"), ""), testTool.apply(i("A"), "Amsg"))));
    }

    @Test
    public void twoFailing() {

        assertEquals("* false: 0 ok\n\n2 failed:\n---\n22: error 1\n44: error 4",
                f.apply(asList(testTool.apply(i("22"), "error 1"), testTool.apply(i("44"), "error 4"))));
    }

    @Test
    public void oneFailingThreeThrowing() {

        assertEquals("* false: 0 ok\n\n1 failed:\n---\n22: \n\nA:\n---\nA: 'Amsg'\n" +
                        "\n" +
                        "B:\n" +
                        "---\n" +
                        "B: 'Bmsg'\n" +
                        "\n" +
                        "C:\n" +
                        "---\n" +
                        "C: 'Cmsg'\n\n\n---\nA:\nA: 'Amsg'\n" +
                        "\n" +
                        "B:\n" +
                        "B: 'Bmsg'\n" +
                        "\n" +
                        "C:\n" +
                        "C: 'Cmsg'\n",
                f.apply(asList(testTool.apply(i("22"), ""), testTool.apply(i("A"), "Amsg"),
                        testTool.apply(i("B"), "Bmsg"),
                        testTool.apply(i("C"), "Cmsg"))));
    }

    @Test
    public void oneSuccessfulOneFailingThreeThrowing() {

        assertEquals("* false: 1 ok\n\n1 failed:\n---\n22: \n\nA:\n---\nA: 'Amsg'\n" +
                        "\n" +
                        "B:\n" +
                        "---\n" +
                        "B: 'Bmsg'\n" +
                        "\n" +
                        "C:\n" +
                        "---\n" +
                        "C: 'Cmsg'\n\n\n---\nA:\nA: 'Amsg'\n" +
                        "\n" +
                        "B:\n" +
                        "B: 'Bmsg'\n" +
                        "\n" +
                        "C:\n" +
                        "C: 'Cmsg'\n",
                f.apply(asList(testTool.apply(i("22"), ""),
                        testTool.apply(i("A"), "Amsg"),
                        testTool.apply(i("B"), "Bmsg"),
                        testTool.apply(i("123"), ""),
                        testTool.apply(i("C"), "Cmsg"))));
    }

    @Test
    public void oneSuccessfulOneFailingSixThrowingSpreadOverThree() {
        assertEquals("* false: 1 ok\n\n1 failed:\n---\n22: \n\nA, A, A:\n---\nA: 'Amsg'\n" +
                        "\n" +
                        "B, B:\n" +
                        "---\n" +
                        "B: 'Bmsg'\n" +
                        "\n" +
                        "C:\n" +
                        "---\n" +
                        "C: 'Cmsg'\n\n\n---\nA:\nA: 'Amsg'\n\nA:\nA: 'Amsg'\n\nA:\nA: 'Amsg'\n" +
                        "\n" +
                        "B:\n" +
                        "B: 'Bmsg'\n" +
                        "\n" +
                        "B:\n" +
                        "B: 'Bmsg'\n" +
                        "\n" +
                        "C:\n" +
                        "C: 'Cmsg'\n",
                f.apply(asList(testTool.apply(i("22"), ""),
                        testTool.apply(i("A"), "Amsg"),
                        testTool.apply(i("B"), "Bmsg"),
                        testTool.apply(i("B"), "Bmsg"),
                        testTool.apply(i("123"), ""),
                        testTool.apply(i("A"), "Amsg"),
                        testTool.apply(i("A"), "Amsg"),
                        testTool.apply(i("C"), "Cmsg"))));

    }

    private DomsItem i(String s) {
        return new DomsItem(new DomsId(s), null);
    }

    public class TestId implements Id {

        private final String id;

        public TestId(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }
    }
}
