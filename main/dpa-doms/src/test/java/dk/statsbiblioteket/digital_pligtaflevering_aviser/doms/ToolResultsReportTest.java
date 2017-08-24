package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolCompletedResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReport;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolThrewExceptionResult;
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

        Id id0 = () -> "*";

        // do not process individually, just count the stream.
        //final Function<Stream<String>, String> successfulRendering = s -> "> " + s.count();

        // just get message instead of printing full stacktrace.
        final Function<Throwable, String> stacktraceRendering = Throwable::getMessage;

        // Fail for id "A","B","C". Otherwise success is if the id.length is an odd length.
        final BiFunction<Id, String, Either<ToolThrewExceptionResult, ToolCompletedResult>> tool =
                (Id id, String output) -> applyOn(id, id1 -> {
                    if (id1.id().equals("A") || id1.id().equals("B") || id1.id().equals("C")) {
                        throw new IllegalArgumentException(id1.id() + ": '" + output + "'");
                    } else {
                        return new ToolCompletedResult(id, (id.id().length() % 2) == 1, output);
                    }
                });

        // We are interested in the number of those successful, the exact id's that were not succesful, and the stacktraces of
        // those that threw an exception.

        BiFunction<List<ToolCompletedResult>, List<ToolCompletedResult>, String> successfulRendering =
                (ok, failed) -> ok.size() + " ok" +
                        (failed.size() > 0
                                ? "\n\nfailed:\n---\n" +
                                failed.stream()
                                        .map(t -> t.id() + ": " + t.getPayload())
                                        .collect(Collectors.joining("\n"))
                                : ""
                        );

        final ToolResultsReport tf = new ToolResultsReport(successfulRendering, stacktraceRendering);

        Function<ToolCompletedResult, String> toString = tr -> tr.getId().id() + " " + tr.isSuccess() + ": " + tr.getPayload();

        Function<List<Either<ToolThrewExceptionResult, ToolCompletedResult>>, String> f = eitherList -> toString.apply(tf.apply(id0, eitherList));

        // -- the below code is hard to read as Java 8 does not support multiline strings.  Note that IntelliJ allows for
        // -- "Alt-Enter->Copy String concatenation text to clipboard" for string concatenations where it
        // -- can be inspected easier.

        assertEquals("* true: 0 ok", f.apply(emptyList()));

        assertEquals("* true: 1 ok", f.apply(singletonList(tool.apply(() -> "1", "x"))));

        assertEquals("* false: 0 ok\n\nfailed:\n---\n22: ", f.apply(singletonList(tool.apply(() -> "22", ""))));

        assertEquals("* false: 0 ok\n\nfailed:\n---\n22: \n\nA:\n---\nA: 'Amsg'\n\n\n---\nA:\nA: 'Amsg'\n",
                f.apply(asList(tool.apply(() -> "22", ""), tool.apply(() -> "A", "Amsg"))));

        assertEquals("* false: 0 ok\n\nfailed:\n---\n22: error 1\n44: error 4",
                f.apply(asList(tool.apply(() -> "22", "error 1"), tool.apply(() -> "44", "error 4"))));

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
                f.apply(asList(tool.apply(() -> "22", ""), tool.apply(() -> "A", "Amsg"),
                        tool.apply(() -> "B", "Bmsg"),
                        tool.apply(() -> "C", "Cmsg"))));

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
                f.apply(asList(tool.apply(() -> "22", ""),
                        tool.apply(() -> "A", "Amsg"),
                        tool.apply(() -> "B", "Bmsg"),
                        tool.apply(() -> "123", ""),
                        tool.apply(() -> "C", "Cmsg"))));
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
                f.apply(asList(tool.apply(() -> "22", ""),
                        tool.apply(() -> "A", "Amsg"),
                        tool.apply(() -> "B", "Bmsg"),
                        tool.apply(() -> "B", "Bmsg"),
                        tool.apply(() -> "123", ""),
                        tool.apply(() -> "A", "Amsg"),
                        tool.apply(() -> "A", "Amsg"),
                        tool.apply(() -> "C", "Cmsg"))));
//
//        assertEquals("> 2", tf.apply(id0, asList(Try.of(() -> "!"), Try.of(() -> " not ok"))));
//
//        // one successful, one failed
//        assertEquals("> 1\n\nA", tf.apply(id0, asList(Try.of(() -> "!"), Try.of(() -> {
//            throw new RuntimeException("A");
//        }))));
//
//        // two successful, one failed.
//        assertEquals("> 2\n\nA", tf.apply(asList(Try.of(() -> "!"), Try.of(() -> {
//            throw new RuntimeException("A");
//        }), Try.of(() -> "!"))));
//
//        // one succesful, three failed.
//        assertEquals("> 1\n\nA\n\nB\n\nC", tf.apply(
//                asList(Try.of(() -> "!"),
//                        Try.of(() -> {
//                            throw new RuntimeException("A");
//                        }), Try.of(() -> {
//                            throw new RuntimeException("B");
//                        }), Try.of(() -> {
//                            throw new RuntimeException("C");
//                        }))
//        ));
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
