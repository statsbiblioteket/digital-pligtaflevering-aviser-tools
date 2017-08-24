package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Id;
import javaslang.control.Either;
import org.junit.Test;

import java.util.List;
import java.util.function.BiFunction;
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

    @Test
    public void test1() {

        DomsItem id0 = i("*");

        // do not process individually, just count the stream.
        //final Function<Stream<String>, String> successfulRendering = s -> "> " + s.count();

        // just get message instead of printing full stacktrace.
        final Function<Throwable, String> stacktraceRendering = Throwable::getMessage;

        // Fail for id "A","B","C". Otherwise success is if the id.length is an odd length.
        final BiFunction<DomsItem, String, Either<ToolThrewExceptionResult, ToolResult>> tool =
                (DomsItem item, String output) -> applyOn(item, item1 -> {
                    if (item1.getDomsId().id().equals("A") || item1.getDomsId().id().equals("B") || item1.getDomsId().id().equals("C")) {
                        throw new IllegalArgumentException(item1.getDomsId().id() + ": '" + output + "'");
                    } else {
                        return new ToolResult(item, (item.getDomsId().id().length() % 2) == 1, output);
                    }
                });

        // We are interested in the number of those successful, the exact id's that were not succesful, and the stacktraces of
        // those that threw an exception.

        BiFunction<List<ToolResult>, List<ToolResult>, String> successfulRendering =
                (ok, failed) -> ok.size() + " ok" +
                        (failed.size() > 0
                                ? "\n\nfailed:\n---\n" +
                                failed.stream()
                                        .map(t -> t.id() + ": " + t.getHumanlyReadableMessage())
                                        .collect(Collectors.joining("\n"))
                                : ""
                        );

        final ToolResultsReport tf = new ToolResultsReport(successfulRendering, stacktraceRendering);

        Function<ToolResult, String> toString = tr -> tr.id() + " " + tr.isSuccess() + ": " + tr.getHumanlyReadableMessage();

        Function<List<Either<ToolThrewExceptionResult, ToolResult>>, String> f = eitherList -> toString.apply(tf.apply(id0, eitherList));

        // -- the below code is hard to read as Java 8 does not support multiline strings.  Note that IntelliJ allows for
        // -- "Alt-Enter->Copy String concatenation text to clipboard" for string concatenations where it
        // -- can be inspected easier.

        assertEquals("* true: 0 ok", f.apply(emptyList()));

        assertEquals("* true: 1 ok", f.apply(singletonList(tool.apply(i("1"), "x"))));

        assertEquals("* false: 0 ok\n\nfailed:\n---\n22: ", f.apply(singletonList(tool.apply(i("22"), ""))));

        assertEquals("* false: 0 ok\n\nfailed:\n---\n22: \n\nA:\n---\nA: 'Amsg'\n\n\n---\nA:\nA: 'Amsg'\n",
                f.apply(asList(tool.apply(i("22"), ""), tool.apply(i("A"), "Amsg"))));

        assertEquals("* false: 0 ok\n\nfailed:\n---\n22: error 1\n44: error 4",
                f.apply(asList(tool.apply(i("22"), "error 1"), tool.apply(i("44"), "error 4"))));

        assertEquals("* false: 0 ok\n\nfailed:\n---\n22: \n\nA:\n---\nA: 'Amsg'\n" +
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
                f.apply(asList(tool.apply(i("22"), ""), tool.apply(i("A"), "Amsg"),
                        tool.apply(i("B"), "Bmsg"),
                        tool.apply(i("C"), "Cmsg"))));

        assertEquals("* false: 1 ok\n\nfailed:\n---\n22: \n\nA:\n---\nA: 'Amsg'\n" +
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
                f.apply(asList(tool.apply(i("22"), ""),
                        tool.apply(i("A"), "Amsg"),
                        tool.apply(i("B"), "Bmsg"),
                        tool.apply(i("123"), ""),
                        tool.apply(i("C"), "Cmsg"))));
        assertEquals("* false: 1 ok\n\nfailed:\n---\n22: \n\nA, A, A:\n---\nA: 'Amsg'\n" +
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
                f.apply(asList(tool.apply(i("22"), ""),
                        tool.apply(i("A"), "Amsg"),
                        tool.apply(i("B"), "Bmsg"),
                        tool.apply(i("B"), "Bmsg"),
                        tool.apply(i("123"), ""),
                        tool.apply(i("A"), "Amsg"),
                        tool.apply(i("A"), "Amsg"),
                        tool.apply(i("C"), "Cmsg"))));

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
