package us.globalforce.resources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.globalforce.services.Sentiment;
import us.globalforce.services.SentimentAnalyzer;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.salesforce.client.AuthToken;
import com.salesforce.client.SObject;
import com.salesforce.client.SObjectList;
import com.salesforce.client.SalesforceClient;

@WebServlet(urlPatterns = { "/DemoREST" })
@Singleton
public class DemoREST extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(DemoREST.class);

    private static final long serialVersionUID = 1L;
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String INSTANCE_URL = "INSTANCE_URL";

    static final Gson gson = new Gson();

    @Inject
    SentimentAnalyzer sentimentAnalyzer;

    @Inject
    HttpClient httpClient;

    private void showAccounts(SalesforceClient client, PrintWriter writer) throws ServletException, IOException {

        SObjectList results = client.runQuery("SELECT Id,Subject,Description from Case LIMIT 100");

        for (SObject o : results) {
            writer.write(o.getId() + ", " + o.getName() + "\n");

            writer.write("<table>");
            for (String key : o.keys()) {
                String value = o.find(key);

                writer.write("<tr><td>");
                writer.write(key);
                writer.write("</td>");

                writer.write("<td>");
                writer.write(value);
                writer.write("</td></tr>");
            }
            writer.write("</table>");

            // Sentiment__c

            StringBuilder text = new StringBuilder();
            String subject = o.find("Subject");
            if (!Strings.isNullOrEmpty(subject)) {
                text.append(subject);
            }
            String description = o.find("Description");
            if (!Strings.isNullOrEmpty(description)) {
                if (text.length() != 0) {
                    text.append("\n");
                }
                text.append(description);
            }

            Sentiment sentiment = sentimentAnalyzer.scoreSentiment(text.toString());
            if (sentiment == null) {
                writer.write("No sentiment");
            } else {
                writer.write("Sentiment: " + sentiment);
            }
        }

        writer.write("\n");
    }

    private String createAccount(String name, String instanceUrl, String accessToken, PrintWriter writer)
            throws ServletException, IOException {
        String accountId = null;

        HttpClient httpclient = new HttpClient();

        JsonObject account = new JsonObject();
        account.addProperty("Name", name);

        PostMethod post = new PostMethod(instanceUrl + "/services/data/v20.0/sobjects/Account/");

        post.setRequestHeader("Authorization", "OAuth " + accessToken);
        post.setRequestEntity(new StringRequestEntity(account.toString(), "application/json", null));

        try {
            httpclient.executeMethod(post);

            writer.write("HTTP status " + post.getStatusCode() + " creating account\n\n");

            if (post.getStatusCode() == HttpStatus.SC_CREATED) {
                JsonParser parser = new JsonParser();

                JsonObject response = parser.parse(new InputStreamReader(post.getResponseBodyAsStream()))
                        .getAsJsonObject();

                System.out.println("Create response: " + response.toString());

                if (response.get("success").getAsBoolean()) {
                    accountId = response.get("id").getAsString();
                    writer.write("New record id " + accountId + "\n\n");
                }
            }
        } finally {
            post.releaseConnection();
        }

        return accountId;
    }

    private void showAccount(String accountId, String instanceUrl, String accessToken, PrintWriter writer)
            throws ServletException, IOException {
        HttpClient httpclient = new HttpClient();
        GetMethod get = new GetMethod(instanceUrl + "/services/data/v20.0/sobjects/Account/" + accountId);

        // set the token in the header
        get.setRequestHeader("Authorization", "OAuth " + accessToken);

        try {
            httpclient.executeMethod(get);
            if (get.getStatusCode() == HttpStatus.SC_OK) {
                JsonParser parser = new JsonParser();

                JsonObject response = parser.parse(new InputStreamReader(get.getResponseBodyAsStream()))
                        .getAsJsonObject();

                System.out.println("Query response: " + response.toString());

                writer.write("Account content\n\n");

                for (Entry<String, JsonElement> entry : response.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue().toString();
                    writer.write(key + ":" + (value != null ? value : "") + "\n");
                }

                writer.write("\n");
            }
        } finally {
            get.releaseConnection();
        }
    }

    private void updateAccount(String accountId, String newName, String city, String instanceUrl, String accessToken,
            PrintWriter writer) throws ServletException, IOException {
        HttpClient httpclient = new HttpClient();

        JsonObject update = new JsonObject();
        update.addProperty("Name", newName);
        update.addProperty("BillingCity", city);

        PostMethod patch = new PostMethod(instanceUrl + "/services/data/v20.0/sobjects/Account/" + accountId) {
            @Override
            public String getName() {
                return "PATCH";
            }
        };

        patch.setRequestHeader("Authorization", "OAuth " + accessToken);
        patch.setRequestEntity(new StringRequestEntity(update.toString(), "application/json", null));

        try {
            httpclient.executeMethod(patch);
            writer.write("HTTP status " + patch.getStatusCode() + " updating account " + accountId + "\n\n");
        } finally {
            patch.releaseConnection();
        }
    }

    private void deleteAccount(String accountId, String instanceUrl, String accessToken, PrintWriter writer)
            throws IOException {
        HttpClient httpclient = new HttpClient();

        DeleteMethod delete = new DeleteMethod(instanceUrl + "/services/data/v20.0/sobjects/Account/" + accountId);

        delete.setRequestHeader("Authorization", "OAuth " + accessToken);

        try {
            httpclient.executeMethod(delete);
            writer.write("HTTP status " + delete.getStatusCode() + " deleting account " + accountId + "\n\n");
        } finally {
            delete.releaseConnection();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter writer = response.getWriter();

        String accessToken = (String) request.getSession().getAttribute(ACCESS_TOKEN);

        String instanceUrl = (String) request.getSession().getAttribute(INSTANCE_URL);

        if (accessToken == null) {
            log.info("No access token; redirecting");
            response.sendRedirect(request.getContextPath() + "/oauth");
            return;
        }

        log.info("We have an access token: " + accessToken + "\n" + "Using instance " + instanceUrl + "\n\n");

        URL url = new URL(instanceUrl);
        SalesforceClient client = new SalesforceClient(httpClient, url, new AuthToken(accessToken));

        showAccounts(client, writer);

        /*
         * String accountId = createAccount("My New Org", instanceUrl, accessToken, writer);
         * 
         * showAccount(accountId, instanceUrl, accessToken, writer);
         * 
         * showAccounts(instanceUrl, accessToken, writer);
         * 
         * updateAccount(accountId, "My New Org, Inc", "San Francisco", instanceUrl, accessToken, writer);
         * 
         * showAccount(accountId, instanceUrl, accessToken, writer);
         * 
         * deleteAccount(accountId, instanceUrl, accessToken, writer);
         * 
         * showAccounts(instanceUrl, accessToken, writer);
         */
    }
}