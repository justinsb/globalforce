package us.globalforce.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.httpclient.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.globalforce.model.Credential;
import us.globalforce.salesforce.client.SObject;
import us.globalforce.salesforce.client.SObjectList;
import us.globalforce.salesforce.client.SalesforceClient;
import us.globalforce.salesforce.client.oauth.OAuthClient;
import us.globalforce.salesforce.client.oauth.OAuthToken;

import com.google.gson.JsonObject;

@Singleton
public class SalesforceUpdater {
    private static final Logger log = LoggerFactory.getLogger(SalesforceUpdater.class);

    @Inject
    OAuthClient oauthClient;

    @Inject
    JdbcRepository repository;

    final ExecutorService executor = Executors.newCachedThreadPool();

    @Inject
    HttpClient httpClient;

    @Inject
    SentimentService sentimentService;

    public void updateSentiment(String organization, final String sfClass, final String objectId, final int sentiment) {
        final Credential credential = repository.findCredential(organization);
        if (credential == null) {
            log.error("Unable to find credential for organization {}", organization);
            return;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    OAuthToken token = oauthClient.refreshToken(credential.refreshToken);

                    SalesforceClient client = new SalesforceClient(httpClient, token);

                    JsonObject update = new JsonObject();
                    update.addProperty("Sentiment__c", sentiment);

                    client.update(sfClass, objectId, update);
                } catch (Exception e) {
                    log.error("Error updating salesforce", e);
                }
            }

        });
    }

    public void catchup(final Credential credential) {
        catchup(credential, "Case");
    }

    public void catchup(final Credential credential, final String sfClass) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    OAuthToken token = oauthClient.refreshToken(credential.refreshToken);

                    SalesforceClient client = new SalesforceClient(httpClient, token);

                    String soql = "SELECT Id FROM " + sfClass + " WHERE Sentiment__c = null";

                    SObjectList list = client.runQuery(soql);

                    for (SObject i : list) {
                        String objectId = i.getId();
                        SObject o = client.find(sfClass, objectId);

                        if (o == null) {
                            log.warn("Unable to find object: {}", objectId);
                        } else {
                            sentimentService.findSentiment(credential.organization, o);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error doing catch-up with salesforce", e);
                }
            }
        });
    }

    public void analyzeObject(final Credential credential, final String sfClass, final String objectId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    OAuthToken token = oauthClient.refreshToken(credential.refreshToken);

                    SalesforceClient client = new SalesforceClient(httpClient, token);

                    SObject o = client.find(sfClass, objectId);

                    if (o == null) {
                        log.warn("Unable to find object: {}", objectId);
                    } else {
                        sentimentService.findSentiment(credential.organization, o);
                    }
                } catch (Exception e) {
                    log.error("Error analyzing object from salesforce", e);
                }
            }
        });
    }

}
