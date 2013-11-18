package us.globalforce.services;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalesforceClient {
	private static final Logger log = LoggerFactory
			.getLogger(SalesforceClient.class);

	final HttpClient httpclient;
	final URL baseUrl;

	final AuthToken token;

	public SalesforceClient(HttpClient httpclient, URL baseUrl, AuthToken token) {
		super();
		this.httpclient = httpclient;
		this.baseUrl = baseUrl;
		this.token = token;
	}

	public SfObjectSchema getSchema(String entity) throws IOException {
		URL url = new URL(baseUrl, "services/data/v20.0/" + entity
				+ "/describe");

		GetMethod get = new GetMethod(url.toString());

		// set the token in the header
		get.setRequestHeader("Authorization", token.getHeader());

		httpclient.executeMethod(get);
		if (get.getStatusCode() == HttpStatus.SC_OK) {
			return new SfObjectSchema(get);
		} else {
			log.info("Bad response fetching schema: {}", get.getStatusLine());
			throw new IOException("Unexpected status code");
		}
	}
}
