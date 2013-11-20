package us.globalforce.salesforce.client.oauth;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Splitter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class OAuthToken implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String accessToken;
    private final String instanceUrl;
    private final String refreshToken;
    private final String idUrl;

    private final String organizationId;
    private final String userId;

    public String getHeader() {
        return "OAuth " + accessToken;
    }

    public OAuthToken(JsonObject authResponse) throws IOException {
        accessToken = authResponse.get("access_token").getAsString();
        instanceUrl = authResponse.get("instance_url").getAsString();
        refreshToken = findString(authResponse, "refresh_token");
        idUrl = authResponse.get("id").getAsString();

        URL u = new URL(idUrl);
        String path = u.getPath();
        List<String> tokens = Splitter.on('/').omitEmptyStrings().splitToList(path);
        if (tokens.get(0).equals("id")) {
            this.organizationId = tokens.get(1);
            this.userId = tokens.get(2);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private String findString(JsonObject json, String property) {
        JsonElement jsonElement = json.get(property);
        if (jsonElement == null) {
            return null;
        }
        return jsonElement.getAsString();
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public URL getInstanceUrl() {
        try {
            return new URL(instanceUrl);
        } catch (MalformedURLException e) {
            throw new IllegalStateException();
        }
    }

    public static OAuthToken find(HttpServletRequest request) {
        OAuthToken accessToken = (OAuthToken) request.getSession().getAttribute(OAuthToken.class.getName());
        return accessToken;
    }

    public void storeInSession(HttpServletRequest request) {
        request.getSession().setAttribute(OAuthToken.class.getName(), this);
    }

    public String getIdUrl() {
        return idUrl;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "OAuthToken [accessToken=" + accessToken + ", instanceUrl=" + instanceUrl + ", refreshToken="
                + refreshToken + ", idUrl=" + idUrl + ", organizationId=" + organizationId + ", userId=" + userId + "]";
    }

}
