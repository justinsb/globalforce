package us.globalforce.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

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

    @GET
    @Path("assign")
    @Produces({ JSON })
    public Task assignTask() throws Exception {
        Task task = repository.assignTask();
        if (task == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        return task;
    }

    @POST
    @Produces({ JSON })
    @Consumes({ JSON })
    public Task addTaskDecision(Task task) throws Exception {
        return repository.addTaskDecision(task);
    }

}
