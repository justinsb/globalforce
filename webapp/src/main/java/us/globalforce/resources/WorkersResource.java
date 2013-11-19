package us.globalforce.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import us.globalforce.model.HumanWorker;
import us.globalforce.services.JdbcRepository;

@Path("/api/workers")
public class WorkersResource extends ResourceBase {
    @Inject
    JdbcRepository repository;

    @GET
    @Produces({ JSON })
    public List<HumanWorker> listWorkers() throws Exception {
        return repository.listHumanWorkers();
    }
}
