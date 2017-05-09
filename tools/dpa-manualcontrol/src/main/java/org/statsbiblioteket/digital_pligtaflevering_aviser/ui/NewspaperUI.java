package org.statsbiblioteket.digital_pligtaflevering_aviser.ui;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration;
import dk.statsbiblioteket.sbutil.webservices.configuration.ConfigCollection;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.SearchPanel;
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
    public static final String AUTONOMOUS_THIS_EVENT = "autonomous.thisEvent";

    private DomsModule domsModule = new DomsModule();
    private ConfigurationMap map = ConfigurationMapHelper.configurationMapFromProperties("/backend.properties");

    private DataModel model = new DataModel();
    /**
     * Initiate the application
     * @param request
     */
    @Override
    protected void init(VaadinRequest request) {

        StatisticsView.fedoraPath = domsModule.provideDomsURL(map) + "/objects/";
        StatisticsView.bitRepoPath = map.getRequired(IngesterConfiguration.BITMAG_BASEURL_PROPERTY) + "var/file1pillar/files/dpaviser/folderDir/";
        StatisticsView.manualCheckEventname = map.getRequired(AUTONOMOUS_THIS_EVENT);

        String initials = request.getUserPrincipal().getName();
        model.setInitials(initials);
        String productionMode = ConfigCollection.getProperties().getProperty("productionMode");

        String month = request.getParameter("month");
        String del = request.getParameter("del");
        String title = request.getParameter("title");
        model.setSelectedMonth(month);
        model.setSelectedDelivery(del);
        model.setSelectedTitle(title);

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
