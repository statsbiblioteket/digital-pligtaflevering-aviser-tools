package org.statsbiblioteket.digital_pligtaflevering_aviser.ui;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import dk.statsbiblioteket.sbutil.webservices.configuration.ConfigCollection;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.views.StatisticsView;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.views.MainView;

import java.util.List;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class NewspaperUI extends UI {
    public static String address = "localhost";
    private Navigator navigator;
    public static final String MAINVIEW = "";
    public static final String CONFIGPANEL = "CONFIGPANEL";
    public static final String DELIVERYPANEL = "DELIVERYPANEL";
    public static final String OVERVIEW = "OVERVIEW";
    public static final String TITLEVALIDATIONPANEL = "TITLEVALIDATIONPANEL";

    private DataModel model = new DataModel();
    /**
     * Initiate the application
     * @param request
     */
    @Override
    protected void init(VaadinRequest request) {

        //Enable to navigate to raw path
        /*if(navigator==null) {
            String thisIsDomainPath = request.getContextPath();
            Page.getCurrent().open(thisIsDomainPath + "/", null);
        }*/

        String initials = request.getUserPrincipal().getName();
        model.setInitials(initials);
        String productionMode = ConfigCollection.getProperties().getProperty("productionMode");

        List<String> groups = (List<String>) request.getAttribute("sbAdGroups");
        address = request.getRemoteAddr();
        getPage().setTitle("DPA");

        // Create a navigator to control the views
        navigator = new Navigator(this, this);

        // Create and register the views
        navigator.addView(MAINVIEW, new MainView());
        navigator.addView(CONFIGPANEL, new StatisticsView(model, CONFIGPANEL));
        navigator.addView(DELIVERYPANEL, new StatisticsView(model, DELIVERYPANEL));
        navigator.addView(TITLEVALIDATIONPANEL, new StatisticsView(model, TITLEVALIDATIONPANEL));
        navigator.addView(OVERVIEW, new StatisticsView(model, OVERVIEW));
    }

}
