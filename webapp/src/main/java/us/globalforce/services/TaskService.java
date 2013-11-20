package us.globalforce.services;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import us.globalforce.model.Task;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Singleton
public class TaskService {
    @Inject
    JdbcRepository repository;

    public Task addTaskDecision(Task decision) {
        return repository.addTaskDecision(decision);
    }

    public List<Task> assignTasks(String organizationId, int n, List<Long> veto) {
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

}
