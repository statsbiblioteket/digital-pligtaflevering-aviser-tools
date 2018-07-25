package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import javax.management.MXBean;

/**
 * Basic MXBean to provide a window to the Tool through a local connection with visualvm/jconsole/jmc.  A default implementation is
 * provided.  If more information is needed, extend this interface and annotate it with @MXBean, and provide a new
 * implementation.
 */
@MXBean
public interface ToolMXBean {

    public static final String JMX_OBJECT_NAME = "jmx.object.name";

    /**
     * What is currently being looked at?
     *
     * @return DOMS identifier of the current item
     */
    String getCurrentId();

    /**
     * A counter of id's processed so far.  Just an index in the query string
     *
     * @return number of DOMS identifiers processed so far.
     */
    long getIdsProcessed();

    /**
     * Details on the current operation intended for a human operator.  Keep the string short (less than 80 chars)
     *
     * @return a humanly readable string.
     */
    String getDetails();

    /**
     * Calculate the average number of ids processed per second.
     */

    double getAverageIdsProcessedPerSecond();
}
