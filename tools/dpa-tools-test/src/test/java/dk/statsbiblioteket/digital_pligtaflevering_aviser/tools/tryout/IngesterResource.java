package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.tryout;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMapHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.IngesterMain;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.IngesterMain.DPA_DELIVERIES_FOLDER;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule.BITREPOSITORY_SBPILLAR_MOUNTPOINT;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule.DATASTREAM_CACHE;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("ingesterResource")
public class IngesterResource {

    private final Tool tool;

    public IngesterResource() {
        this.tool = new IngesterMain().getTool(ConfigurationMapHelper.configurationMapFromProperties("ingester.properties")
                .add(ConfigurationMapHelper.configurationMapFromKeyValueStrings(DPA_DELIVERIES_FOLDER + "=/home/tra/git/digital-pligtaflevering-aviser-tools/delivery-samples",
                        BITREPOSITORY_SBPILLAR_MOUNTPOINT + "=/home/tra/git/digital-pligtaflevering-aviser-tools/bitrepositorystub-storage",
                        BITMAG_BASEURL_PROPERTY + "=http://localhost:58709/",
                        "pageSize=9999",
                        DATASTREAM_CACHE + "=INGESTER_CACHE")));
    }

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() throws Exception {
        return tool.call().toString();
    }
}
