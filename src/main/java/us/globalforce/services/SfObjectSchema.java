package us.globalforce.services;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.methods.GetMethod;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SfObjectSchema {
	final JsonObject schema;

	public SfObjectSchema(GetMethod get) throws IOException {
		JsonParser parser = new JsonParser();
		JsonObject response = parser.parse(
				new InputStreamReader(get.getResponseBodyAsStream()))
				.getAsJsonObject();

		this.schema = response;
	}

}
