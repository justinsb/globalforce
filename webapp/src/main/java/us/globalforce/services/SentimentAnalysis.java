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
        double sumX3 = 0;
        double sumX2 = 0;
        double sumX = 0;
        double n = 0;

        for (Sentiment sentiment : sentiments) {
            if (sentiment == null) {
                continue;
            }
            double s = sentiment.getScore();
            sumX += s;
            sumX2 += s * s;
            sumX3 += s * s * s;
            n++;
        }

        // int measure = Math.round(sumX / n);
        int measure = (int) Math.round(Math.pow(sumX3 / n, 1.0 / 3.0));
        return Sentiment.fromScore(measure);
    }
}
