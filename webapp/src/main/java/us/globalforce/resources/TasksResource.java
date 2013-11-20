package us.globalforce.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import us.globalforce.model.Task;
import us.globalforce.salesforce.client.oauth.OAuthToken;
import us.globalforce.services.TaskService;

@Path("/api/tasks")
public class TasksResource extends ResourceBase {
    @Inject
    TaskService taskService;

    // @GET
    // @Produces({ JSON })
    // public List<Task> listOpenTasks() throws Exception {
    // OAuthToken oauthToken = getAuthToken();
    // String organizationId = oauthToken.getOrganizationId();
    // return repository.listAllOpenTasks(organizationId);
    // }

    @GET
    @Path("assign")
    @Produces({ JSON })
    public List<Task> assignTask(@QueryParam("n") @DefaultValue("5") int n, @QueryParam("veto") List<Long> veto)
            throws Exception {
        OAuthToken oauthToken = getAuthToken();
        String organizationId = oauthToken.getOrganizationId();

        return taskService.assignTasks(organizationId, n, veto);

    }

    @POST
    @Produces({ JSON })
    @Consumes({ JSON })
    public Task addTaskDecision(Task task) throws Exception {
        OAuthToken oauthToken = getAuthToken();

        task.worker = oauthToken.getUserId();
        task.organization = oauthToken.getOrganizationId();

        return taskService.addTaskDecision(task);
    }
}
