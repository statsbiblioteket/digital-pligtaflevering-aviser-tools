package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.tryout;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.IngesterMain;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collections;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("ingesterResource")
public class IngesterResource {

    private final Tool tool;

    public IngesterResource() {
        this.tool = new IngesterMain().getTool(new ConfigurationMap(Collections.emptyMap()));
    }

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return tool.toString();
    }
}
