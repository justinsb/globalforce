package us.globalforce.salesforce.client;

import com.google.gson.JsonObject;

public class SField {
    private final JsonObject json;
    private final String name;
    private final String type;
    private final String label;

    SField(JsonObject json) {
        this.json = json;

        this.name = json.get("name").getAsString();
        this.type = json.get("name").getAsString();
        this.label = json.get("label").getAsString();
    }

    // "fields" :
    // [
    // {
    // "length" : 18,
    // "name" : "Id",
    // "type" : "id",
    // "defaultValue" : { "value" : null },
    // "updateable" : false,
    // "label" : "Account ID",
    // ...
    // },
}
