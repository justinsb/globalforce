package us.globalforce.services;

import java.util.List;

public class SentimentAnalysis {
    final List<String> sentences;
    final List<Sentiment> sentiments;

    public SentimentAnalysis(List<String> sentences, List<Sentiment> sentiments) {
        this.sentences = sentences;
        this.sentiments = sentiments;
    }

    public List<String> getSentences() {
        return sentences;
    }

    public List<Sentiment> getSentiments() {
        return sentiments;
    }

    public boolean isComplete() {
        for (Sentiment sentiment : sentiments) {
            if (sentiment == null) {
                return false;
            }
        }
        return true;
    }

    public Sentiment getOverall() {
        float sumX2 = 0;
        float sumX = 0;
        float n = 0;

        for (Sentiment sentiment : sentiments) {
            if (sentiment == null) {
                continue;
            }
            float s = sentiment.getScore();
            sumX += s;
            sumX2 += s * s;
            n++;
        }

        int mean = Math.round(sumX / n);

        return Sentiment.fromScore(mean);
    }
}
