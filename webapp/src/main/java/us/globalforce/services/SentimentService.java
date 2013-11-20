package us.globalforce.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.globalforce.model.Task;
import us.globalforce.salesforce.client.SObject;

import com.google.common.collect.Lists;

@Singleton
public class SentimentService {
    private static final Logger log = LoggerFactory.getLogger(SentimentService.class);

    private static final String FIELD_SENTIMENT = "Sentiment__c";

    @Inject
    SentimentAnalyzer sentimentAnalyzer;

    @Inject
    JdbcRepository repository;

    public Sentiment findSentiment(String organizationId, SObject o) {
        Object sentimentValue = o.find(FIELD_SENTIMENT);

        if (sentimentValue != null) {
            // TODO: Store the value in Salesforce and retrieve it!
            log.error("Sentiment value set on: {}", o);

            String s = sentimentValue.toString();
            if (s.equals("-2")) {
                return Sentiment.STRONG_NEGATIVE;
            } else if (s.equals("-1")) {
                return Sentiment.NEGATIVE;
            } else if (s.equals("0")) {
                return Sentiment.NEUTRAL;
            } else if (s.equals("1")) {
                return Sentiment.POSITIVE;
            } else if (s.equals("2")) {
                return Sentiment.STRONG_POSITIVE;
            } else {
                throw new IllegalStateException();
            }
        }

        String objectId = o.getId();

        log.info("Checking sentiment for {}", objectId);

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
                repository.addTask(organizationId, ProblemType.Sentiment, o.getSfClass(), objectId, i, sentence);
            }
            return null;
        } else {
            return determineSentiment(tasks);
        }
    }

    public Sentiment determineSentiment(List<Task> tasks) {
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
        return null;
    }

}
