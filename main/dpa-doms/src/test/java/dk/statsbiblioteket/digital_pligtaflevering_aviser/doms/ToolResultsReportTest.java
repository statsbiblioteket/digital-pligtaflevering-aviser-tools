package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.kb.stream.StreamTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReport.OK_COUNT_FAIL_LIST_RENDERER;
import io.vavr.control.Either;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Check that the ToolResultsReport actually invokes the passed-in string converter to generate a report for the
 * non-failing Try-entries, and that the invoked StackTrace renderer method is actually invoked for each failing
 * Try-entry with a blank line between each and that the final report consist of these two concatenated.
 *
 * @noinspection WeakerAccess, ArraysAsListWithZeroOrOneArgument
 */
public class ToolResultsReportTest {

    protected DomsItem id0 = i("X");
    protected DomsItem id1 = i("*1");
    protected DomsItem id2 = i("2*");
    protected DomsItem id3 = i("3");

    // We are interested in the number of those successful, the exact id's that were not succesful, and the stacktraces of
    // those that threw an exception.

    protected BiConsumer<DomsItem, Exception> noLogging = (item, exception) -> {
        // we do not want to actually log the captured exceptions
    };

    /**
     * We must test actual stack traces.  As code may be moving around, it is necessary to normalize line numbers and
     * lambda expression names (which typically contain a counter.  Also we cut off at three lines to keep expected
     * strings relatively small, and not dive into the JVM classes.
     */
    protected Function<Throwable, String> twoLineAnonymousStackTraceRenderer = e -> {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String st = sw.toString();
        pw.close();
        // Now make the stack trace two lines max, and set the line number to 0 to make more robust against change.
        // Package names are still present, so test will break if they are renamed (I could not get the regexp
        // to remove the package name to work).
        List<String> lines = new BufferedReader(new StringReader(st)).lines().limit(3).collect(toList());
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            line = replaceNumbersWithZero(line);
            lines.set(i, line);
        }
        return String.join("\n", lines);
    };

    /**
     * (ToolResult.java:60) -> (ToolResult.java:0)
     */

    protected String replaceNumbersWithZero(String line) {
        return line.replaceAll("\\d+", "0");
    }

    /**
     * Capture the "evaluate something and return it wrapped in an Either.right if successful, or return a thrown
     * exception as Either.left if not successful.
     */

    @Deprecated
    public static <C> Either<Exception, C> either(Callable<C> c) {
        try {
            return Either.right(c.call());
        } catch (Exception e) {
            return Either.left(e);
        }
    }

    @Test
    public void testReplacementWithZeros() {
        assertEquals("null$0", replaceNumbersWithZero("null$1234"));
        assertEquals("(Foo.java:0)", replaceNumbersWithZero("(Foo.java:1234)"));

        String s1 = "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReportTest.lambda$null$26(ToolResultsReportTest.java:123)\n";
        String s2 = "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReportTest.lambda$null$0(ToolResultsReportTest.java:0)\n";

        assertEquals(s2, replaceNumbersWithZero(s1));
        assertEquals(s2, replaceNumbersWithZero(replaceNumbersWithZero(s1))); // just to be sure.
    }

    @Test
    public void noItems() {

        ToolResult report = new ToolResultsReport<>(new OK_COUNT_FAIL_LIST_RENDERER<>(), noLogging, Throwable::getMessage).apply(id0, Stream.<DomsItem>of()
                .map(StreamTuple::create)
                .map(c -> c.map(v -> either(() -> ToolResult.ok("" + v))))
                .collect(toList()));

        assertTrue("isSuccess()", report.isSuccess());
        assertEquals("getHumanlyReadableMessage()", "0 ok", report.getHumanlyReadableMessage());
    }

    @Test
    public void singleOkItems() {

        ToolResult report = new ToolResultsReport<>(new OK_COUNT_FAIL_LIST_RENDERER<>(), noLogging, Throwable::getMessage).apply(id0, Stream.of(id1)
                .map(StreamTuple::create)
                .map(c -> c.map(v -> either(() -> ToolResult.ok("" + v))))
                .collect(toList()));

        assertTrue("isSuccess()", report.isSuccess());
        assertEquals("getHumanlyReadableMessage()", "1 ok", report.getHumanlyReadableMessage());
    }

    @Test
    public void twoOKItems() {

        ToolResult report = new ToolResultsReport<>(new OK_COUNT_FAIL_LIST_RENDERER<>(), noLogging, Throwable::getMessage).apply(id0, Stream.of(id1, id2)
                .map(StreamTuple::create)
                .map(c -> c.map(v -> either(() -> ToolResult.ok("" + v))))
                .collect(toList()));

        assertTrue("isSuccess()", report.isSuccess());
        assertEquals("getHumanlyReadableMessage()", "2 ok", report.getHumanlyReadableMessage());
    }

    @Test
    public void singleOKsingleFailItems() {

        ToolResult report = new ToolResultsReport<>(new OK_COUNT_FAIL_LIST_RENDERER<>(), noLogging, Throwable::getMessage).apply(id0, Stream.of(id1, id2)
                .map(StreamTuple::create)
                .map(c -> c.map(v -> either(() -> v.equals(id1) ? ToolResult.ok("OK:" + v) : ToolResult.fail("FAIL:" + v))))
                .collect(toList()));

        assertFalse("isSuccess()", report.isSuccess());
        assertEquals("getHumanlyReadableMessage()", "1 ok\n" +
                "\n" +
                "1 failed:\n" +
                "=========\n" +
                "2*: FAIL:2*", report.getHumanlyReadableMessage());
    }

    @Test
    public void twoFailItems() {

        ToolResult report = new ToolResultsReport<>(new OK_COUNT_FAIL_LIST_RENDERER<>(), noLogging, Throwable::getMessage).apply(id0, Stream.of(id1, id2)
                .map(StreamTuple::create)
                .map(c -> c.map(v -> either(() -> ToolResult.fail("FAIL:" + v))))
                .collect(toList()));

        assertFalse("isSuccess()", report.isSuccess());
        assertEquals("getHumanlyReadableMessage()", "0 ok\n" +
                "\n" +
                "2 failed:\n" +
                "=========\n" +
                "*1: FAIL:*1\n" +
                "2*: FAIL:2*", report.getHumanlyReadableMessage());
    }

    @Test
    public void singleOKtwoFailItems() {

        ToolResult report = new ToolResultsReport<>(new OK_COUNT_FAIL_LIST_RENDERER<>(), noLogging, Throwable::getMessage).apply(id0, Stream.of(id1, id2, id3)
                .map(StreamTuple::create)
                .map(c -> c.map(v -> either(() -> v.equals(id1) ? ToolResult.ok("OK:" + v) : ToolResult.fail("FAIL:" + v))))
                .collect(toList()));

        assertFalse("isSuccess()", report.isSuccess());
        assertEquals("getHumanlyReadableMessage()", "1 ok\n" +
                "\n" +
                "2 failed:\n" +
                "=========\n" +
                "2*: FAIL:2*\n" +
                "3: FAIL:3", report.getHumanlyReadableMessage());
    }

    @Test
    public void singleOKsingleFailSingleExceptionItems() {

        ToolResult report = new ToolResultsReport<>(new OK_COUNT_FAIL_LIST_RENDERER<>(), noLogging, Throwable::getMessage).apply(id0, Stream.of(id1, id2, id3)
                .map(StreamTuple::create)
                .map(c -> c.map(v -> either(() -> v.equals(id1) ? ToolResult.ok("OK:" + v) : v.equals(id2) ? ToolResult.fail("FAIL:" + v) : ToolResult.hurl("HURL:" + v))))
                .collect(toList()));

        assertFalse("isSuccess()", report.isSuccess());
        assertEquals("getHumanlyReadableMessage()", "1 ok\n" +
                "\n" +
                "1 failed:\n" +
                "=========\n" +
                "2*: FAIL:2*\n" +
                "\n" +
                "3:\n" +
                "---\n" +
                "HURL:3\n" +
                "\n" +
                "\n" +
                "---\n" +
                "3:\n" +
                "HURL:3\n", report.getHumanlyReadableMessage());
    }

    @Test
    public void threeDifferentExceptionNoStacktracesItems() {

        ToolResult report = new ToolResultsReport<>(new OK_COUNT_FAIL_LIST_RENDERER<>(), noLogging, Throwable::getMessage).apply(id0, Stream.of(id1, id2, id3)
                .map(StreamTuple::create)
                .map(c -> c.map(v -> either(() -> ToolResult.hurl("HURL:" + v))))
                .collect(toList()));

        assertFalse("isSuccess()", report.isSuccess());
        assertEquals("getHumanlyReadableMessage()", "0 ok\n" +
                "\n" +
                "*1:\n" +
                "---\n" +
                "HURL:*1\n" +
                "\n" +
                "2*:\n" +
                "---\n" +
                "HURL:2*\n" +
                "\n" +
                "3:\n" +
                "---\n" +
                "HURL:3\n" +
                "\n" +
                "\n" +
                "---\n" +
                "*1:\n" +
                "HURL:*1\n" +
                "\n" +
                "2*:\n" +
                "HURL:2*\n" +
                "\n" +
                "3:\n" +
                "HURL:3\n", report.getHumanlyReadableMessage());
    }

    @Test
    public void threeIdenticalExceptionNoStacktracesItems() {

        ToolResult report = new ToolResultsReport<>(new OK_COUNT_FAIL_LIST_RENDERER<>(), noLogging, twoLineAnonymousStackTraceRenderer)
                .apply(id0, Stream.of(id1, id1, id1)
                        .map(StreamTuple::create)
                        .map(c -> c.map(v -> either(() -> ToolResult.hurl("HURL:" + v))))
                        .collect(toList()));

        assertFalse("isSuccess()", report.isSuccess());
        // will break if package names are refactored.
        assertEquals("getHumanlyReadableMessage()", "0 ok\n" +
                        "\n" +
                        "*1, *1, *1:\n" +
                        "---\n" +
                        "java.lang.RuntimeException: HURL:*1\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult.hurl(ToolResult.java:0)\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReportTest.lambda$null$0(ToolResultsReportTest.java:0)\n" +
                        "\n" +
                        "\n" +
                        "---\n" +
                        "*1:\n" +
                        "java.lang.RuntimeException: HURL:*1\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult.hurl(ToolResult.java:0)\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReportTest.lambda$null$0(ToolResultsReportTest.java:0)\n" +
                        "\n" +
                        "*1:\n" +
                        "java.lang.RuntimeException: HURL:*1\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult.hurl(ToolResult.java:0)\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReportTest.lambda$null$0(ToolResultsReportTest.java:0)\n" +
                        "\n" +
                        "*1:\n" +
                        "java.lang.RuntimeException: HURL:*1\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult.hurl(ToolResult.java:0)\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReportTest.lambda$null$0(ToolResultsReportTest.java:0)\n",
                report.getHumanlyReadableMessage());
    }

    @Test
    public void twoIdenticalOneDifferentExceptionNoStacktracesItems() {

        ToolResult report = new ToolResultsReport<>(new OK_COUNT_FAIL_LIST_RENDERER<>(), noLogging, twoLineAnonymousStackTraceRenderer)
                .apply(id0, Stream.of(id1, id2, id1)
                        .map(StreamTuple::create)
                        .map(c -> c.map(v -> either(() -> ToolResult.hurl("HURL:" + v))))
                        .collect(toList()));

        assertFalse("isSuccess()", report.isSuccess());
        // will break if package names are refactored.
        assertEquals("getHumanlyReadableMessage()", "0 ok\n" +
                        "\n" +
                        "*1, *1:\n" +
                        "---\n" +
                        "java.lang.RuntimeException: HURL:*1\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult.hurl(ToolResult.java:0)\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReportTest.lambda$null$0(ToolResultsReportTest.java:0)\n" +
                        "\n" +
                        "2*:\n" +
                        "---\n" +
                        "java.lang.RuntimeException: HURL:2*\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult.hurl(ToolResult.java:0)\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReportTest.lambda$null$0(ToolResultsReportTest.java:0)\n" +
                        "\n" +
                        "\n" +
                        "---\n" +
                        "*1:\n" +
                        "java.lang.RuntimeException: HURL:*1\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult.hurl(ToolResult.java:0)\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReportTest.lambda$null$0(ToolResultsReportTest.java:0)\n" +
                        "\n" +
                        "*1:\n" +
                        "java.lang.RuntimeException: HURL:*1\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult.hurl(ToolResult.java:0)\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReportTest.lambda$null$0(ToolResultsReportTest.java:0)\n" +
                        "\n" +
                        "2*:\n" +
                        "java.lang.RuntimeException: HURL:2*\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult.hurl(ToolResult.java:0)\n" +
                        "\tat dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReportTest.lambda$null$0(ToolResultsReportTest.java:0)\n",
                report.getHumanlyReadableMessage());
    }

    private DomsItem i(String s) {
        return new DomsItem(new DomsId(s), null);
    }

}
