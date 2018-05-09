package org.statsbiblioteket.digital_pligtaflevering_aviser.ui;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.Settings;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.views.StatisticsView;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class NewspaperUI extends UI {

    protected Logger log = LoggerFactory.getLogger(getClass());

    public static String address = "localhost";
    private Navigator navigator;
    public static final String MAINVIEW = "";
    public static final String CONFIGPANEL = "CONFIGPANEL";
    public static final String DELIVERYPANEL = "DELIVERYPANEL";
    public static final String OVERVIEW = "OVERVIEW";
    public static final String FRONTPAGE = "FRONTPAGE";
    public static final String TITLEVALIDATIONPANEL = "TITLEVALIDATIONPANEL";

    private DataModel model = new DataModel();

    /**
     * Initiate the application
     *
     * @param request
     */
    @Override
    protected void init(VaadinRequest request) {

        if (request.getUserPrincipal() != null) {
            String initials = request.getUserPrincipal().getName();
            model.setInitials(initials);
        }

        String screenwidth = request.getParameter("screenwidth");
        if(screenwidth!=null) {
            Settings.screenwidth = Integer.parseInt(screenwidth);
        }
        //These parameters can be used to construct a link to information without performing the search in the UI
        String month = request.getParameter("month");
        String del = request.getParameter("del");
        String title = request.getParameter("title");
        boolean validated = "true".equals(request.getParameter("validated")); //This parameter is a backdoor to view information that is normally hidden from the user

        model.setSelectedMonth(month);
        model.setSelectedDelivery(del);
        model.setSelectedTitle(title);
        model.setIncludeValidatedDeliveries(validated);

        address = request.getRemoteAddr();
        getPage().setTitle("DPA");

        // Create a navigator to control the views
        navigator = new Navigator(this, this);

        StatisticsView deliveryPanel = new StatisticsView(model, DELIVERYPANEL);

        // Create and register the views
        navigator.addView(MAINVIEW, deliveryPanel);
        navigator.addView(CONFIGPANEL, new StatisticsView(model, CONFIGPANEL));
        navigator.addView(DELIVERYPANEL, deliveryPanel);
        navigator.addView(TITLEVALIDATIONPANEL, new StatisticsView(model, TITLEVALIDATIONPANEL));
        navigator.addView(OVERVIEW, new StatisticsView(model, OVERVIEW));
        navigator.addView(FRONTPAGE, new StatisticsView(model, FRONTPAGE));
    }

}
