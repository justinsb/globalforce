package us.globalforce.services;

import java.util.List;
import java.util.Properties;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

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

    public Sentiment scoreSentiment(List<String> sections) {
        // Synchronized because CoreNLP not thread safe
        synchronized (pipeline) {
            float sumX2 = 0;
            float sumX = 0;
            float n = 0;

            for (String text : sections) {
                if (Strings.isNullOrEmpty(text)) {
                    continue;
                }

                text = text.trim();
                if (Strings.isNullOrEmpty(text)) {
                    continue;
                }

                Annotation annotation = pipeline.process(text);
                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
                    int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                    if (sentiment >= 0 && sentiment <= 4) {
                        float s = sentiment;
                        sumX += s;
                        sumX2 += s * s;
                        n++;

                        log.debug("Sentiment: {} for {}", sentiment, sentence.toString());
                    }
                }
            }

            if (n == 0) {
                return null;
            }

            int mean = Math.round(sumX / n);

            switch (mean) {
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