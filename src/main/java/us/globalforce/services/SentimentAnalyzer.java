package us.globalforce.services;

import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class SentimentAnalyzer {
	public Sentiment scoreSentiment(String text) {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		float sum = 0;
		float n = 0;

		if (text != null && text.length() > 0) {
			Annotation annotation = pipeline.process(text);
			for (CoreMap sentence : annotation
					.get(CoreAnnotations.SentencesAnnotation.class)) {
				Tree tree = sentence
						.get(SentimentCoreAnnotations.AnnotatedTree.class);
				int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
				if (sentiment >= 0 && sentiment <= 3) {
					sum += sentiment;
					n++;
				}
			}
		}

		if (n == 0) {
			return null;
		}

		int average = (int) Math.round(sum / n);

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