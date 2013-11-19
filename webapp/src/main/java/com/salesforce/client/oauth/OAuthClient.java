package com.salesforce.client.oauth;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OAuthClient {
    private static final Logger log = LoggerFactory.getLogger(OAuthClient.class);

    private final String clientId = "3MVG9A2kN3Bn17ht1Sa_5M8pmOHZuFU98yx.VxDUG7qkW9pqUk7c9tX57iXvSAB1k9VSbECGOaB79S_Agel0d";
    private final String clientSecret = "295020390184049994";
    private final String redirectUri = "https://pacific-gorge-1278.herokuapp.com/oauth/_callback";
    private final String environment = "https://login.salesforce.com";
    private final HttpClient httpClient;

    URL getTokenValidateUrl() {
        try {
            return new URL(environment + "/services/oauth2/token");
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

    public OAuthClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public OAuthToken validate(String code) throws IOException {
        PostMethod post = new PostMethod(getTokenValidateUrl().toString());
        post.addParameter("code", code);
        post.addParameter("grant_type", "authorization_code");
        post.addParameter("client_id", clientId);
        post.addParameter("client_secret", clientSecret);
        post.addParameter("redirect_uri", redirectUri);

        try {
            httpClient.executeMethod(post);

            JsonParser parser = new JsonParser();
            JsonObject authResponse = parser.parse(new InputStreamReader(post.getResponseBodyAsStream()))
                    .getAsJsonObject();
            log.info("Auth response: " + authResponse.toString());

            OAuthToken token = new OAuthToken(authResponse);

            log.info("Got access token: " + token);

            return token;
        } finally {
            post.releaseConnection();
        }
    }

    public URL getAuthUrl() {
        try {
            String authUrl = environment + "/services/oauth2/authorize?response_type=code&client_id=" + clientId
                    + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");
            return new URL(authUrl);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException();
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

}
