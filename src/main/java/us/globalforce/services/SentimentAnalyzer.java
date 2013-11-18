package us.globalforce.services;

import java.util.Properties;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

@Singleton
public class SentimentAnalyzer {
    private static final Logger log = LoggerFactory.getLogger(SentimentAnalyzer.class);

    final StanfordCoreNLP pipeline;

    public SentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");

        // Paragraphs are definitely sentences
        props.setProperty("ssplit.newlineIsSentenceBreak", "two");

        pipeline = new StanfordCoreNLP(props);
    }

    public Sentiment scoreSentiment(String text) {
        // Synchronized because CoreNLP not thread safe
        synchronized (this) {
            float sum = 0;
            float n = 0;

            if (text != null && text.length() > 0) {
                Annotation annotation = pipeline.process(text);
                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
                    int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                    if (sentiment >= 0 && sentiment <= 4) {
                        sum += sentiment;
                        n++;

                        log.debug("Sentiment: {} for {}", sentiment, sentence.toString());
                    }
                }
            }

            if (n == 0) {
                return null;
            }

            int average = Math.round(sum / n);

            switch (average) {
            case 0:
                return Sentiment.STRONG_NEGATIVE;
            case 1:
                return Sentiment.NEGATIVE;
            case 2:
                return Sentiment.NEUTRAL;
            case 3:
                return Sentiment.POSITIVE;
            case 4:
                return Sentiment.STRONG_POSITIVE;
            default:
                return null;
            }
        }
    }
}