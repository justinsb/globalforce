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

    @Inject
    SentimentService sentimentService;

    @Inject
    SalesforceUpdater salesforceUpdater;

    public Task addTaskDecision(Task decision) {
        Task task = repository.addTaskDecision(decision);

        List<Task> tasks = repository.listTasks(decision.organization, decision.problem, decision.objectId);

        Sentiment sentiment = sentimentService.determineSentiment(tasks);
        if (sentiment != null) {
            salesforceUpdater.updateSentiment(decision.organization, decision.sfClass, decision.objectId,
                    sentiment.getScore());
        }

        return task;
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
