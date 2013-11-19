package us.globalforce.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import us.globalforce.model.HumanWorker;
import us.globalforce.services.JdbcRepository;

@Path("/api/workers")
public class WorkersResource {
    @Inject
    JdbcRepository repository;

    @GET
    public List<HumanWorker> listWorkers() throws Exception {
        return repository.listHumanWorkers();
    }
}
