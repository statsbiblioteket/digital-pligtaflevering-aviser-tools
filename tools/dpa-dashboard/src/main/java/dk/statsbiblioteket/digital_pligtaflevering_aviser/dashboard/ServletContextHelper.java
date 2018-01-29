package dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard;

import com.google.common.base.Throwables;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.enterprise.inject.Produces;

/**
 *
 */
public class ServletContextHelper {

    /**
     * Return a map of init parameters from the passed in servletcontext.
     *
     * @param servletContext current context, typical location in
     * request.getServletContext()
     * @return corresponding map.
     */
    public static Map<String, String> getInitParameterMap(ServletContext servletContext) {
        Map<String, String> map = new HashMap<>();
        for (String name : Collections.list(servletContext.getInitParameterNames())) {
            map.put(name, servletContext.getInitParameter(name));
        }
        return map;
    }

    @Produces
    public ConfigurationMap getConfigurationMapFromServletContextInitParameters(ServletContext sc) {
        return new ConfigurationMap(getInitParameterMap(sc));
    }

    @Produces
    public <K> BiConsumer<K, Exception> getStacktraceForToolResultsReportTest(ServletContext sc) {
        return (k, e) -> sc.log("Item: " + k, e);
    }

}
