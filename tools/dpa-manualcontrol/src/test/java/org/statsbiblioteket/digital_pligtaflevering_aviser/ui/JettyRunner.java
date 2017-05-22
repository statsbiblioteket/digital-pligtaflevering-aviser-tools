package org.statsbiblioteket.digital_pligtaflevering_aviser.ui;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * Runner for stating the application during development
 */
public class JettyRunner {


    public static void main(String[] args) throws Exception {

        // Create Jetty Server
        Server server = new Server(8080);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/dpa-manualcontrol");
        webapp.setInitParameter("autonomous.sboi.url", "http://localhost:58608/newspapr/sbsolr/");
        webapp.setInitParameter("doms.username", "fedoraAdmin");
        webapp.setInitParameter("doms.password", "fedoraAdminPass");
        webapp.setInitParameter("doms.pidgenerator.url", "http://localhost:7880/pidgenerator-service");
        webapp.setInitParameter("doms.url", "http://localhost:7880/fedora");
        webapp.setInitParameter("pageSize", "10");
        webapp.setInitParameter("bitrepository.ingester.baseurl", "http://localhost:58709/");
        webapp.setInitParameter("autonomous.pastSuccessfulEvents", "Data_Archived,Statistics_generated");
        webapp.setInitParameter("autonomous.itemTypes", "doms:ContentModel_DPARoundTrip");
        webapp.setInitParameter("autonomous.sboi.pageSize", "100");
        webapp.setInitParameter("autonomous.futureEvents", "ManualValidationDone");
        webapp.setInitParameter("autonomous.thisEvent", "ManualValidationDone");
        webapp.setInitParameter("autonomous.component.fedoraRetries", "10");
        webapp.setInitParameter("autonomous.component.fedoraDelayBetweenRetries", "10");
        webapp.setInitParameter("dpa.manualcontrol.cashingfolder", "/tmp/");
        webapp.setInitParameter("doms.collection.pid", "doms_sboi_dpaCollection");
        webapp.setInitParameter("casServerUrlPrefix", "https://samling.statsbiblioteket.dk/casinternal/");
        webapp.setInitParameter("casServerLoginUrl", "https://samling.statsbiblioteket.dk/casinternal/");
        webapp.setInitParameter("serverName", "localhost");
        webapp.setInitParameter("service", "http://localhost:8080/dpa-manualcontrol/VAADIN;mode=manualvalidate");

        webapp.setWar("/home/mmj/projects/digital-pligtaflevering-aviser-tools/tools/dpa-manualcontrol/target/dpa-manualcontrol.war");
        server.setHandler(webapp);

        server.start();
        server.join();

        server.dumpStdErr();
    }
}