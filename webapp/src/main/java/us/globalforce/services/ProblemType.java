package us.globalforce.services;

public enum ProblemType {
    Sentiment("S");

    final String key;

    private ProblemType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
