package dk.statsbiblioteket.digital_pligtaflevering_aviser.dashboard;

import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ServletContextHelper {
    /** Return a map of init parameters from the passed in servletcontext.
     *
     * @param servletContext current context, typical location in request.getServletContext()
     * @return corresponding map.
     */
    public static Map<String, String> getInitParameterMap(ServletContext servletContext) {
        Map<String, String> map = new HashMap<>();
        for(String name : Collections.list(servletContext.getInitParameterNames())) {
            map.put(name, servletContext.getInitParameter(name));
        }
        return map;
    }
}
