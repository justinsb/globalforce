package us.globalforce.resources;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import com.salesforce.client.oauth.OAuthToken;

public class ResourceBase {
    public static final String JSON = javax.ws.rs.core.MediaType.APPLICATION_JSON;
    public static final String XML = javax.ws.rs.core.MediaType.APPLICATION_XML;
    public static final String TEXT_PLAIN = javax.ws.rs.core.MediaType.TEXT_PLAIN;

    @Inject
    HttpServletRequest request;

    public OAuthToken findAuthToken() {
        return OAuthToken.find(request);
    }

    public OAuthToken getAuthToken() {
        OAuthToken authToken = findAuthToken();
        if (authToken == null) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
        return authToken;
    }
}
