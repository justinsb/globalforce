package us.globalforce.services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.methods.GetMethod;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class SObjectList implements Iterable<SObject> {
	final List<SObject> results;

	public SObjectList(GetMethod get) throws JsonIOException,
			JsonSyntaxException, IOException {
		JsonParser parser = new JsonParser();
		JsonObject response = parser.parse(
				new InputStreamReader(get.getResponseBodyAsStream()))
				.getAsJsonObject();

		this.results = parse(response);
	}

	private List<SObject> parse(JsonObject response) {
		List<SObject> objects = Lists.newArrayList();

		JsonArray records = response.get("records").getAsJsonArray();

		for (int i = 0; i < results.size(); i++) {
			objects.add(new SObject(records.get(i).getAsJsonObject()));
		}

		return objects;
	}

	@Override
	public Iterator<SObject> iterator() {
		return results.iterator();
	}

}
