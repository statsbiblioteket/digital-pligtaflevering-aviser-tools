package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import javax.inject.Inject;
import javax.inject.Named;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>This bean have public fields for easy manipulation, with getters to be exposed as MXBean fields in jconsole/visualvm.
 * It autoregisters itself with JMX (this may or may not be a good idea - time will tell).
 * </p>
 * <p>See <a href="http://www.oracle.com/technetwork/java/javase/tech/best-practices-jsp-136021.html">http://www.oracle.com/technetwork/java/javase/tech/best-practices-jsp-136021.html</a>
 * for details</p>
 *
 */
public class DefaultToolMXBean implements ToolMXBean {

    // FIXME:  @Singletons are hard in dagger, for now add a unique identifier to the objectName
    static AtomicLong instanceCounter = new AtomicLong(0);

    private final long startTime;


    @Inject
    //@Singleton
    public DefaultToolMXBean(@Named(JMX_OBJECT_NAME) String jmxObjectName) {
        this.startTime = System.currentTimeMillis();
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName(jmxObjectName + ",name=" + instanceCounter.addAndGet(1));
            mbs.registerMBean(this, name);
        } catch (JMException e) {
            throw new RuntimeException("jmxObjectName=" + jmxObjectName, e);
        }
    }

    public String details;
    public String currentId;
    public long idsProcessed = 0;

    @Override
    public String getCurrentId() {
        return currentId;
    }

    @Override
    public long getIdsProcessed() {
        return idsProcessed;
    }

    @Override
    public String getDetails() {
        return details;
    }

    @Override
    public double getAverageIdsProcessedPerSecond() {
        return idsProcessed * 1000.0 / (System.currentTimeMillis() - startTime);
    }
}
