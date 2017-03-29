package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import javax.management.MBeanServer;
import javax.management.MXBean;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Date;

public class JMXInvestigationLauncher {

    public static void main(String[] args) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("_com.example:type=Hello");
        Object mbean = new SampleBean();
        mbs.registerMBean(mbean, name);

        System.out.println("Waiting forever...");
        Thread.sleep(Long.MAX_VALUE);
    }

    private static class SampleBean implements SampleMXBean {
        public String getString1() {
            return "String 1 " + new Date();
        }
        public String getString2() {
            return "String 2 " + new Date();
        }
        public String string3(int i, long j) {
            return "String " + i + " " + j + new Date();
        }
    }

    @MXBean
    public interface SampleMXBean {
        String getString1();
        String getString2();
        String string3(int i, long j);
    }

}
