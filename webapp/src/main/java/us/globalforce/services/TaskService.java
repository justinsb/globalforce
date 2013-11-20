package us.globalforce.services;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import us.globalforce.model.Task;

import com.google.common.base.Strings;

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
        List<Task> tasks = repository.assignTasks(organizationId, n, veto);
        for (Task task : tasks) {
            task.input = mask(task.input);
        }
        return tasks;
    }

    private String mask(String s) {
        if (Strings.isNullOrEmpty(s)) {
            return s;
        }

        // Replace any digits by #
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c)) {
                sb.append("#");
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

}
