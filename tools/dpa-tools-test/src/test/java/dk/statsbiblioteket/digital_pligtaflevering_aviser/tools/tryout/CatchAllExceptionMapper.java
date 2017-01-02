package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.tryout;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * http://stackoverflow.com/a/15185615/53897
 */
@Provider
public class CatchAllExceptionMapper implements ExceptionMapper<Exception>{
    @Override
    public Response toResponse(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        pw.close();
        return Response.status(Response.Status.NOT_FOUND).entity(new Date() + "\n\n" + sw).type("text/plain").build();
    }
}
