package org.statsbiblioteket.digital_pligtaflevering_aviser.ui;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import dk.statsbiblioteket.sbutil.webservices.configuration.ConfigCollection;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.views.StatisticsView;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.views.StatusView;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.views.MainView;


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
    protected static final String STATISTICSVIEW1 = "1v1";
    protected static final String STATISTICSVIEW2 = "1v2";
    protected static final String STATISTICSVIEW3 = "2v1";
    protected static final String STATISTICSVIEW4 = "2v2";


    FetchEventStructure eventStructureCommunication = new FetchEventStructure();
    DataModel model = new DataModel();

    @Override
    protected void init(VaadinRequest request) {

        String productionMode = ConfigCollection.getProperties().getProperty("productionMode");

        address = request.getRemoteAddr();

        getPage().setTitle("Navigation Example");

        // Create a navigator to control the views
        navigator = new Navigator(this, this);

        // Create and register the views
        navigator.addView(MAINVIEW, new MainView());
        navigator.addView(STATUSVIEW, new StatusView());
        navigator.addView(STATISTICSVIEW1, new StatisticsView(STATISTICSVIEW1));
        navigator.addView(STATISTICSVIEW2, new StatisticsView(STATISTICSVIEW2));
        navigator.addView(STATISTICSVIEW3, new StatisticsView(STATISTICSVIEW3));
        navigator.addView(STATISTICSVIEW4, new StatisticsView(STATISTICSVIEW4));
    }

}
