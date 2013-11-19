package us.globalforce.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/api/ping")
public class RootResource {
    @GET
    public String ping() {
        return "pong";
    }
}
