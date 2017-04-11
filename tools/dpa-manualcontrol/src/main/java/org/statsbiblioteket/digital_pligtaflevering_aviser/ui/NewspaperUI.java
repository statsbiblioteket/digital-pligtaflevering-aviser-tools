package org.statsbiblioteket.digital_pligtaflevering_aviser.ui;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper;
import dk.statsbiblioteket.sbutil.webservices.configuration.ConfigCollection;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers.RepositoryConfigurator;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.views.StatisticsView;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.views.MainView;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class NewspaperUI extends UI {
    public static String address = "localhost";
    private Navigator navigator;
    public static final String MAINVIEW = "";
    public static final String DELIVERYPANEL = "DELIVERYPANEL";
    public static final String TITLEVALIDATIONPANEL = "TITLEVALIDATIONPANEL";



    @Override
    protected void init(VaadinRequest request) {

        String productionMode = ConfigCollection.getProperties().getProperty("productionMode");

        address = request.getRemoteAddr();
        getPage().setTitle("DPA");

        // Create a navigator to control the views
        navigator = new Navigator(this, this);

        // Create and register the views
        navigator.addView(MAINVIEW, new MainView());
        navigator.addView(DELIVERYPANEL, new StatisticsView(DELIVERYPANEL));
        navigator.addView(TITLEVALIDATIONPANEL, new StatisticsView(TITLEVALIDATIONPANEL));
    }

}
