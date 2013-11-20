package us.globalforce.salesforce.client;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class SObject {

    private static final Logger log = LoggerFactory.getLogger(SObject.class);

    private final JsonObject json;

    public SObject(JsonObject json) {
        this.json = json;
    }

    public String getId() {
        return getString("Id");
    }

    public Set<String> keys() {
        Set<String> keys = Sets.newHashSet();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            keys.add(key);
        }

        return keys;
    }

    public String getString(String key) {
        String value = findString(key, null);
        if (value == null) {
            throw new IllegalArgumentException("Value not found: " + key);
        }
        return value;
    }

    public Object find(String key) {
        JsonElement jsonElement = json.get(key);
        if (jsonElement == null) {
            return null;
        }
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive primitive = (JsonPrimitive) jsonElement;
            if (primitive.isString()) {
                return primitive.getAsString();
            } else {
                log.warn("Key was found, but was not known-type: {}={}", key, jsonElement);
            }
        } else if (jsonElement.isJsonNull()) {
            return null;
        } else {
            return jsonElement;
        }
        return null;
    }

    public String findString(String key, String defaultValue) {
        JsonElement jsonElement = json.get(key);
        Object value = find(key);

        if (value != null) {
            if (value instanceof String) {
                return (String) value;
            }

            log.warn("Key was found, but was not string: {}={}", key, jsonElement);
        }
        return defaultValue;
    }
}
