package com.salesforce.client.oauth;

import com.google.gson.JsonObject;

public class OAuthToken {

    private final String accessToken;
    private final String instanceUrl;

    public OAuthToken(JsonObject authResponse) {
        accessToken = authResponse.get("access_token").getAsString();
        instanceUrl = authResponse.get("instance_url").getAsString();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getInstanceUrl() {
        return instanceUrl;
    }

}
