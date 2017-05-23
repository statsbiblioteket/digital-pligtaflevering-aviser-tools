package org.statsbiblioteket.digital_pligtaflevering_aviser.ui;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;

/**
 * Contextlistener for the application, this runs once when the application is started up
 */
@WebListener( "Context listener for doing something or other." )
public class NewspaperContextListener implements ServletContextListener {

    public static final String AUTONOMOUS_THIS_EVENT = "autonomous.thisEvent";

    public static ConfigurationMap configurationmap = new ConfigurationMap(new HashMap<>());
    public static String fedoraPath;
    public static String manualCheckEventname;

    // Initialize applicationparameters once as static parameters, for access from all over the application
    @Override
    public void contextInitialized ( ServletContextEvent contextEvent ) {
        ServletContext servletContext = contextEvent.getServletContext();

        //ServletContext servletContext = contextEvent.getServletContext();
        Map<String, String> attributeMap = new HashMap<>();
        for(Object name : Collections.list(servletContext.getInitParameterNames())) {
            attributeMap.put(name.toString(), servletContext.getInitParameter(name.toString()));
        }
        configurationmap = new ConfigurationMap(attributeMap);
        fedoraPath = configurationmap.getRequired(DOMS_URL) + "/objects/";
        manualCheckEventname = configurationmap.getRequired(AUTONOMOUS_THIS_EVENT);
    }

    // For releasing of recources during shutdown
    @Override
    public void contextDestroyed ( ServletContextEvent contextEvent ) {

    }

}