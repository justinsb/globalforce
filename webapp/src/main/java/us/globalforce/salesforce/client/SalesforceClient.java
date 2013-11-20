package us.globalforce.salesforce.client;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.globalforce.salesforce.client.oauth.OAuthToken;

public class SalesforceClient {
    private static final Logger log = LoggerFactory.getLogger(SalesforceClient.class);

    final HttpClient httpclient;
    final URL baseUrl;

    final OAuthToken token;

    public SalesforceClient(HttpClient httpclient, URL baseUrl, OAuthToken token) {
        super();
        this.httpclient = httpclient;
        this.baseUrl = baseUrl;
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
        URL url = new URL(baseUrl, "services/data/v20.0/query");

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
                log.info("Bad response fetching schema: {}", get.getStatusLine());
                throw new IOException("Unexpected status code");
            }
        } finally {
            get.releaseConnection();
        }
    }

    public OAuthToken getAuthToken() {
        return this.token;
    }
}
