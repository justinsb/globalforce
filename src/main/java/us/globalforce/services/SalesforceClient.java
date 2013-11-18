package us.globalforce.services;

import java.net.URL;

import org.apache.commons.httpclient.HttpClient;

public class SalesforceClient {
	final HttpClient httpclient;
	final URL baseUrl;

	public SalesforceClient(HttpClient httpclient, URL baseUrl) {
		super();
		this.httpclient = httpclient;
		this.baseUrl = baseUrl;
	}

}
