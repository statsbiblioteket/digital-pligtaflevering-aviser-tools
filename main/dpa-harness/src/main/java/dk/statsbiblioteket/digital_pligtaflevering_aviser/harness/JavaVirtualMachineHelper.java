package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * JVM specific helper routines.  Currently Hotspot only.
 */
public class JavaVirtualMachineHelper {
    public static void dumpHeap(Function<LocalDateTime, String> heapDumpFileNameFunction) {
        // adapted from https://blogs.oracle.com/sundararajan/entry/programmatically_dumping_heap_from_java
        try {
            // dump everything or only live objects?  For post-mortem everything is interesting.
            boolean onlyLiveVariables = false;
            String heapDumpFileName = heapDumpFileNameFunction.apply(LocalDateTime.now());
            dumpHeap0(heapDumpFileName, onlyLiveVariables);
            LoggerFactory.getLogger(JavaVirtualMachineHelper.class).info("Wrote {}", heapDumpFileName);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("dumpHProf()", e);
        }
    }

    private static void dumpHeap0(String heapDumpFileName, boolean onlyLiveVariables) throws ClassNotFoundException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
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
