package us.globalforce.salesforce.client;

import java.io.IOException;

import com.google.gson.JsonObject;

public class PushTopic {
    public static SObject find(SalesforceClient client, String key) throws IOException {
        String soql = "SELECT Name,Query FROM PushTopic WHERE Name='" + key + "'";
        SObjectList items = client.runQuery(soql);
        return items.firstOrNull();
    }

    public static String create(SalesforceClient client, String key, String query, boolean notifyForCreate,
            boolean notifyForUpdate, boolean notifyForOperationDelete, boolean notifyForOperationUndelete)
            throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("ApiVersion", "29.0");
        json.addProperty("Name", key);
        json.addProperty("Query", query);

        json.addProperty("NotifyForOperationCreate", notifyForCreate);
        json.addProperty("NotifyForOperationUpdate", notifyForUpdate);
        json.addProperty("NotifyForOperationDelete", notifyForOperationDelete);
        json.addProperty("NotifyForOperationUndelete", notifyForOperationUndelete);

        return client.create("PushTopic", json);
    }
}
