package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * Request JVM to create a heap dump (currently HotSpot only).
 */
public class HeapDumpHelper {
    public static void dumpHeap(Function<LocalDateTime, String> heapDumpFileNameFunction) {
        // adapted from https://blogs.oracle.com/sundararajan/entry/programmatically_dumping_heap_from_java
        try {
            // dump everything or only live objects?
            boolean onlyLiveVariables = false;
            heapDump0(heapDumpFileNameFunction.apply(LocalDateTime.now()), onlyLiveVariables);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("dumpHeap()", e);
        }
    }

    private static void heapDump0(String heapDumpFileName, boolean onlyLiveVariables) throws ClassNotFoundException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        Object bean = ManagementFactory.newPlatformMXBeanProxy(
                server,
                "com.sun.management:type=HotSpotDiagnostic",
                clazz);
        // https://docs.oracle.com/javase/8/docs/jre/api/management/extension/com/sun/management/HotSpotDiagnosticMXBean.html#dumpHeap-java.lang.String-boolean-
        Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
        m.invoke(bean, heapDumpFileName, onlyLiveVariables);
    }
}
