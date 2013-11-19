package us.globalforce.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import us.globalforce.model.Task;
import us.globalforce.services.JdbcRepository;

@Path("/api/tasks")
public class TasksResource extends ResourceBase {
    @Inject
    JdbcRepository repository;

    @GET
    @Produces({ JSON })
    public List<Task> listOpenTasks() throws Exception {
        return repository.listAllOpenTasks();
    }
}
