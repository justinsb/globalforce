package us.globalforce.services;

import com.google.common.base.Strings;

public enum Sentiment {
    STRONG_NEGATIVE(-2), NEGATIVE(-1), NEUTRAL(0), POSITIVE(1), STRONG_POSITIVE(2);

    final int code;

    private Sentiment(int code) {
        this.code = code;
    }

    public static Sentiment parse(String decision) {
        if (Strings.isNullOrEmpty(decision)) {
            return null;
        }
        int value = Integer.valueOf(decision);
        return fromScore(value);
    }

    public static Sentiment fromScore(int value) {
        for (Sentiment sentiment : Sentiment.values()) {
            if (sentiment.code == value) {
                return sentiment;
            }
        }
        return null;
    }

    public int getScore() {
        return code;
    }
}
