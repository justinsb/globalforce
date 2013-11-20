package us.globalforce.resources;

import java.util.List;
import java.util.Set;

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
import us.globalforce.services.JdbcRepository;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Path("/api/tasks")
public class TasksResource extends ResourceBase {
    @Inject
    JdbcRepository repository;

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

        List<Task> tasks = Lists.newArrayList();

        Set<Long> ids = Sets.newHashSet();

        for (Long v : veto) {
            ids.add(v);
        }

        for (int i = 0; i < n * 2; i++) {
            Task task = repository.assignTask(organizationId);
            if (task == null) {
                continue;
            }

            if (ids.contains(task.id)) {
                continue;
            }

            tasks.add(task);
            ids.add(task.id);

            if (tasks.size() >= n) {
                break;
            }
        }

        return tasks;
    }

    @POST
    @Produces({ JSON })
    @Consumes({ JSON })
    public Task addTaskDecision(Task task) throws Exception {
        OAuthToken oauthToken = getAuthToken();

        task.worker = oauthToken.getUserId();
        task.organization = oauthToken.getOrganizationId();

        return repository.addTaskDecision(task);
    }
}
