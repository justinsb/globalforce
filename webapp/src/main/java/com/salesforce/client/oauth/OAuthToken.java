package com.salesforce.client.oauth;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;

public class OAuthToken implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public static OAuthToken find(HttpServletRequest request) {
        OAuthToken accessToken = (OAuthToken) request.getSession().getAttribute(OAuthToken.class.getName());
        return accessToken;
    }

    public void storeInSession(HttpServletRequest request) {
        request.getSession().setAttribute(OAuthToken.class.getName(), this);
    }

}
