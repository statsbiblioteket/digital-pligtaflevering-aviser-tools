package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import javaslang.control.Try;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 *  This bean have public fields for easy manipulation, with getters to be exposed as MXBean fields in jconsole/visualvm.
 *  It autoregisters itself with JMX (this may or may not be a good idea - time will tell).
 */
public class DefaultToolMXBean implements ToolMXBean {

    private final long startTime;

    @Inject
    @Singleton
    public DefaultToolMXBean(@Named(JMX_OBJECT_NAME) String jmxObjectName) {
        this.startTime = System.currentTimeMillis();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Try.run(() -> {
            ObjectName name = new ObjectName(jmxObjectName);
            mbs.registerMBean(this, name);
        });
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
