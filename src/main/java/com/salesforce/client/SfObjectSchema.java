package com.salesforce.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.httpclient.methods.GetMethod;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SfObjectSchema {
    final JsonObject schema;
    final List<SField> fields;

    public SfObjectSchema(GetMethod get) throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject response = parser.parse(new InputStreamReader(get.getResponseBodyAsStream())).getAsJsonObject();

        this.schema = response;

        this.fields = Lists.newArrayList();

        JsonArray jsonFields = schema.get("fields").getAsJsonArray();
        for (int i = 0; i < jsonFields.size(); i++) {
            JsonObject jsonField = jsonFields.get(i).getAsJsonObject();
            this.fields.add(new SField(jsonField));
        }
    }

}
