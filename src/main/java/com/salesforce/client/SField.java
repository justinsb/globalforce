package com.salesforce.client;

import com.google.gson.JsonObject;

public class SField {
    private final JsonObject json;

    SField(JsonObject json) {
        this.json = json;
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
