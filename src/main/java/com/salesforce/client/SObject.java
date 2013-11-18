package com.salesforce.client;

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
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive primitive = (JsonPrimitive) jsonElement;
            if (primitive.isString()) {
                return primitive.getAsString();
            } else {
                log.warn("Key was found, but was not json string: {}={}", key, jsonElement);
            }
        } else {
            log.warn("Key was found, but was not json primitive: {}={}", key, jsonElement);
        }
        return null;
    }
}
