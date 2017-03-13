package org.kb.ui;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import dk.statsbiblioteket.sbutil.webservices.configuration.ConfigCollection;
import org.kb.ui.views.MainView;
import org.kb.ui.views.StatisticsView;
import org.kb.ui.views.StatusView;


/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class NewspaperUI extends UI
{

    public static String address = "localhost";

    Navigator navigator;
    protected static final String MAINVIEW = "";
    protected static final String STATUSVIEW = "status";
    protected static final String STATISTICSVIEW = "statistics";
    protected static final String STATISTICSVIEW2 = "statistics2";


    FetchEventStructure eventStructureCommunication = new FetchEventStructure();
    DataModel model = new DataModel();

    @Override
    protected void init(VaadinRequest request) {

        ConfigCollection.getProperties().getProperty("productionMode");

        address = request.getRemoteAddr();

        getPage().setTitle("Navigation Example");

        // Create a navigator to control the views
        navigator = new Navigator(this, this);

        // Create and register the views
        navigator.addView(MAINVIEW, new MainView());
        navigator.addView(STATUSVIEW, new StatusView());
        navigator.addView(STATISTICSVIEW, new StatisticsView("Std"));
        navigator.addView(STATISTICSVIEW2, new StatisticsView("Other"));
    }

}
