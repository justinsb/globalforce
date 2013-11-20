package us.globalforce.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import us.globalforce.model.Task;

import com.google.common.collect.Lists;
import com.salesforce.client.SObject;

@Singleton
public class SentimentService {
    private static final String FIELD_SENTIMENT = "Sentiment__c";

    @Inject
    SentimentAnalyzer sentimentAnalyzer;

    @Inject
    JdbcRepository repository;

    public Sentiment findSentiment(String organizationId, SObject o) {
        Object sentimentValue = o.find(FIELD_SENTIMENT);

        if (sentimentValue != null) {
            // TODO: Store the value in Salesforce and retrieve it!
            throw new UnsupportedOperationException();
        }

        String objectId = o.getId();

        List<Task> tasks = repository.listTasks(organizationId, ProblemType.Sentiment, objectId);
        if (tasks.isEmpty()) {
            List<String> sections = Lists.newArrayList();

            sections.add(o.findString("Subject", ""));
            sections.add(o.findString("Description", ""));

            SentimentAnalysis analysis = sentimentAnalyzer.scoreSentiment(sections);

            List<String> sentences = analysis.getSentences();
            for (int i = 0; i < sentences.size(); i++) {
                String sentence = sentences.get(i);
                // Sentiment sentiment = analysis.getSentiments().get(i);

                // TODO: Once we have more confidence in the model, only create tasks where we're not sure
                repository.addTask(organizationId, ProblemType.Sentiment, objectId, i, sentence);
            }
        } else {
            Collections.sort(tasks, new Comparator<Task>() {
                @Override
                public int compare(Task o1, Task o2) {
                    int s1 = o1.sequence;
                    int s2 = o2.sequence;

                    if (s1 <= s2) {
                        if (s1 == s2) {
                            return 0;
                        } else {
                            return -1;
                        }
                    } else {
                        return 1;
                    }
                }
            });

            List<String> sentences = Lists.newArrayList();
            List<Sentiment> sentiments = Lists.newArrayList();

            for (Task task : tasks) {
                sentences.add(task.input);
                Sentiment sentiment = null;
                if (task.decision != null) {
                    sentiment = Sentiment.parse(task.decision);
                }
                sentiments.add(sentiment);
            }

            SentimentAnalysis analysis = new SentimentAnalysis(sentences, sentiments);
            if (analysis.isComplete()) {
                return analysis.getOverall();
            }
        }

        return null;
    }

}
