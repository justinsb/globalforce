package us.globalforce.services;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SObject {

	private final JsonObject json;

	public SObject(JsonObject json) {
		this.json = json;
	}

	public String getId() {
		return find("Id");
	}

	public String getName() {
		return find("Name");
	}

	public Set<String> keys() {
		Set<String> keys = Sets.newHashSet();
		for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
			String key = entry.getKey();
			keys.add(key);
		}

		return keys;
	}

	public String find(String key) {
		JsonElement jsonElement = json.get(key);
		if (jsonElement == null) {
			return null;
		}
		return jsonElement.toString();
	}

}
