package us.globalforce.salesforce.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.globalforce.salesforce.client.oauth.OAuthToken;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SalesforceClient {
    private static final Logger log = LoggerFactory.getLogger(SalesforceClient.class);

    final HttpClient httpclient;
    final URL baseUrl;

    final OAuthToken token;

    public SalesforceClient(HttpClient httpclient, OAuthToken token) {
        super();
        this.httpclient = httpclient;
        this.baseUrl = token.getInstanceUrl();
        this.token = token;
    }

    public SObjectSchema getSchema(String entity) throws IOException {
        URL url = new URL(baseUrl, "services/data/v20.0/" + entity + "/describe");

        GetMethod get = new GetMethod(url.toString());

        // set the token in the header
        get.setRequestHeader("Authorization", token.getHeader());

        httpclient.executeMethod(get);
        if (get.getStatusCode() == HttpStatus.SC_OK) {
            return new SObjectSchema(get);
        } else {
            log.info("Bad response fetching schema: {}", get.getStatusLine());
            throw new IOException("Unexpected status code");
        }
    }

    public SObjectList runQuery(String soql) throws IOException {
        URL url = new URL(baseUrl, "services/data/v29.0/query");

        GetMethod get = new GetMethod(url.toString());

        try {
            get.setRequestHeader("Authorization", token.getHeader());

            // set the SOQL as a query param
            NameValuePair[] params = new NameValuePair[1];

            params[0] = new NameValuePair("q", soql);
            get.setQueryString(params);

            log.info("Running API query: {}", soql);

            httpclient.executeMethod(get);
            if (get.getStatusCode() == HttpStatus.SC_OK) {
                SObjectList results = new SObjectList(get);
                return results;
            } else {
                log.info("Bad response running query: {}", get.getStatusLine());
                throw new IOException("Unexpected status code");
            }
        } finally {
            get.releaseConnection();
        }
    }

    public OAuthToken getAuthToken() {
        return this.token;
    }

    public void update(String sfClass, String objectId, JsonObject update) throws IOException {
        URL url = new URL(baseUrl, "services/data/v29.0/sobjects/" + sfClass + "/" + objectId);

        PostMethod patch = new PostMethod(url.toString()) {
            @Override
            public String getName() {
                return "PATCH";
            }
        };

        patch.setRequestHeader("Authorization", token.getHeader());
        patch.setRequestEntity(new StringRequestEntity(update.toString(), "application/json", null));

        try {
            httpclient.executeMethod(patch);
            log.info("HTTP status {} updating {}", patch.getStatusCode(), objectId);
        } finally {
            patch.releaseConnection();
        }
    }

    public String create(String sfClass, JsonObject json) throws IOException {
        URL url = new URL(baseUrl, "services/data/v29.0/sobjects/" + sfClass + "/");

        PostMethod post = new PostMethod(url.toString());
        post.setRequestHeader("Authorization", token.getHeader());
        post.setRequestEntity(new StringRequestEntity(json.toString(), "application/json", null));

        try {
            httpclient.executeMethod(post);

            if (post.getStatusCode() == HttpStatus.SC_CREATED) {
                JsonParser parser = new JsonParser();

                JsonObject response = parser.parse(new InputStreamReader(post.getResponseBodyAsStream()))
                        .getAsJsonObject();

                log.info("Create response: {}", response.toString());

                if (response.get("success").getAsBoolean()) {
                    String id = response.get("id").getAsString();
                    log.info("Created item with id {}", id);
                    return id;
                }
            } else {
                log.info("Unexpected status code from create: {}", post.getStatusLine());
            }
        } finally {
            post.releaseConnection();
        }

        throw new IOException("Error creating item");
    }

}
