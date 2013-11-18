package us.globalforce.resources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.globalforce.services.SObject;
import us.globalforce.services.SObjectList;
import us.globalforce.services.Sentiment;
import us.globalforce.services.SentimentAnalyzer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet(urlPatterns = { "/DemoREST" })
public class DemoREST extends HttpServlet {
	private static final Logger log = LoggerFactory.getLogger(DemoREST.class);

	private static final long serialVersionUID = 1L;
	private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
	private static final String INSTANCE_URL = "INSTANCE_URL";

	static final Gson gson = new Gson();

	private void showAccounts(String instanceUrl, String accessToken,
			PrintWriter writer) throws ServletException, IOException {
		HttpClient httpclient = new HttpClient();
		GetMethod get = new GetMethod(instanceUrl
				+ "/services/data/v20.0/query");

		// set the token in the header
		get.setRequestHeader("Authorization", "OAuth " + accessToken);

		// set the SOQL as a query param
		NameValuePair[] params = new NameValuePair[1];

		params[0] = new NameValuePair(
				"q",
				"SELECT Id, Name, Sentiment__c, Subject, Description from Case WHERE Sentiment__c == null LIMIT 100");
		params[0] = new NameValuePair("q",
				"SELECT Id, Name, Sentiment__c, Subject, Description from Case LIMIT 100");
		get.setQueryString(params);

		log.info("Running API query: {}", params[0]);

		SentimentAnalyzer analyzer = new SentimentAnalyzer();

		try {
			httpclient.executeMethod(get);
			if (get.getStatusCode() == HttpStatus.SC_OK) {
				SObjectList results = new SObjectList(get);
				//
				// // Now lets use the standard java json classes to work with
				// the
				// // results
				// JsonParser parser = new JsonParser();
				//
				// JsonObject response = parser.parse(
				// new InputStreamReader(get.getResponseBodyAsStream()))
				// .getAsJsonObject();
				// System.out.println("Query response: " + response.toString());
				//
				// writer.write(response.get("totalSize").getAsString()
				// + " record(s) returned\n\n");

				// JsonArray results = response.get("records").getAsJsonArray();

				// for (int i = 0; i < results.size(); i++) {
				// writer.write(results.get(i).getAsJsonObject().get("Id")
				// .getAsString()
				// + ", "
				// + results.get(i).getAsJsonObject().get("Name")
				// .getAsString() + "\n");
				//
				// writer.write("<table>");
				// for (Entry<String, JsonElement> entry : response.entrySet())
				// {
				// String key = entry.getKey();
				// String value = entry.getValue().toString();
				//
				// writer.write("<tr><td>");
				// writer.write(key);
				// writer.write("</td>");
				//
				// writer.write("<td>");
				// writer.write(value);
				// writer.write("</td></tr>");
				// }
				// writer.write("</table>");
				// }

				for (SObject o : results) {
					writer.write(o.getId() + ", " + o.getName() + "\n");

					writer.write("<table>");
					for (String key : o.keys()) {
						String value = o.get(key);

						writer.write("<tr><td>");
						writer.write(key);
						writer.write("</td>");

						writer.write("<td>");
						writer.write(value);
						writer.write("</td></tr>");
					}
					writer.write("</table>");

					// Sentiment__c

					String text = o.get("Subject");
					text += "\n";
					text += o.get("Description");

					Sentiment sentiment = analyzer.scoreSentiment(text);
					if (sentiment == null) {
						writer.write("No sentiment");
					} else {
						writer.write("Sentiment: " + sentiment);
					}
				}

				writer.write("\n");
			} else {
				log.info("Unexpected API response: {}", get.getStatusLine());
			}
		} finally {
			get.releaseConnection();
		}
	}

	private String createAccount(String name, String instanceUrl,
			String accessToken, PrintWriter writer) throws ServletException,
			IOException {
		String accountId = null;

		HttpClient httpclient = new HttpClient();

		JsonObject account = new JsonObject();
		account.addProperty("Name", name);

		PostMethod post = new PostMethod(instanceUrl
				+ "/services/data/v20.0/sobjects/Account/");

		post.setRequestHeader("Authorization", "OAuth " + accessToken);
		post.setRequestEntity(new StringRequestEntity(account.toString(),
				"application/json", null));

		try {
			httpclient.executeMethod(post);

			writer.write("HTTP status " + post.getStatusCode()
					+ " creating account\n\n");

			if (post.getStatusCode() == HttpStatus.SC_CREATED) {
				JsonParser parser = new JsonParser();

				JsonObject response = parser.parse(
						new InputStreamReader(post.getResponseBodyAsStream()))
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

	private void showAccount(String accountId, String instanceUrl,
			String accessToken, PrintWriter writer) throws ServletException,
			IOException {
		HttpClient httpclient = new HttpClient();
		GetMethod get = new GetMethod(instanceUrl
				+ "/services/data/v20.0/sobjects/Account/" + accountId);

		// set the token in the header
		get.setRequestHeader("Authorization", "OAuth " + accessToken);

		try {
			httpclient.executeMethod(get);
			if (get.getStatusCode() == HttpStatus.SC_OK) {
				JsonParser parser = new JsonParser();

				JsonObject response = parser.parse(
						new InputStreamReader(get.getResponseBodyAsStream()))
						.getAsJsonObject();

				System.out.println("Query response: " + response.toString());

				writer.write("Account content\n\n");

				for (Entry<String, JsonElement> entry : response.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue().toString();
					writer.write(key + ":" + (value != null ? value : "")
							+ "\n");
				}

				writer.write("\n");
			}
		} finally {
			get.releaseConnection();
		}
	}

	private void updateAccount(String accountId, String newName, String city,
			String instanceUrl, String accessToken, PrintWriter writer)
			throws ServletException, IOException {
		HttpClient httpclient = new HttpClient();

		JsonObject update = new JsonObject();
		update.addProperty("Name", newName);
		update.addProperty("BillingCity", city);

		PostMethod patch = new PostMethod(instanceUrl
				+ "/services/data/v20.0/sobjects/Account/" + accountId) {
			@Override
			public String getName() {
				return "PATCH";
			}
		};

		patch.setRequestHeader("Authorization", "OAuth " + accessToken);
		patch.setRequestEntity(new StringRequestEntity(update.toString(),
				"application/json", null));

		try {
			httpclient.executeMethod(patch);
			writer.write("HTTP status " + patch.getStatusCode()
					+ " updating account " + accountId + "\n\n");
		} finally {
			patch.releaseConnection();
		}
	}

	private void deleteAccount(String accountId, String instanceUrl,
			String accessToken, PrintWriter writer) throws IOException {
		HttpClient httpclient = new HttpClient();

		DeleteMethod delete = new DeleteMethod(instanceUrl
				+ "/services/data/v20.0/sobjects/Account/" + accountId);

		delete.setRequestHeader("Authorization", "OAuth " + accessToken);

		try {
			httpclient.executeMethod(delete);
			writer.write("HTTP status " + delete.getStatusCode()
					+ " deleting account " + accountId + "\n\n");
		} finally {
			delete.releaseConnection();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter writer = response.getWriter();

		String accessToken = (String) request.getSession().getAttribute(
				ACCESS_TOKEN);

		String instanceUrl = (String) request.getSession().getAttribute(
				INSTANCE_URL);

		if (accessToken == null) {
			log.info("No access token; redirecting");
			response.sendRedirect(request.getContextPath() + "/oauth");
			return;
		}

		log.info("We have an access token: " + accessToken + "\n"
				+ "Using instance " + instanceUrl + "\n\n");

		showAccounts(instanceUrl, accessToken, writer);

		/*
		 * String accountId = createAccount("My New Org", instanceUrl,
		 * accessToken, writer);
		 * 
		 * showAccount(accountId, instanceUrl, accessToken, writer);
		 * 
		 * showAccounts(instanceUrl, accessToken, writer);
		 * 
		 * updateAccount(accountId, "My New Org, Inc", "San Francisco",
		 * instanceUrl, accessToken, writer);
		 * 
		 * showAccount(accountId, instanceUrl, accessToken, writer);
		 * 
		 * deleteAccount(accountId, instanceUrl, accessToken, writer);
		 * 
		 * showAccounts(instanceUrl, accessToken, writer);
		 */
	}
}