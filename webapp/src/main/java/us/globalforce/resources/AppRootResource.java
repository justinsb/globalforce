package us.globalforce.resources;

import java.net.URI;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.salesforce.client.oauth.OAuthClient;
import com.salesforce.client.oauth.OAuthToken;

@Path("/")
public class AppRootResource {

    private static final Logger log = LoggerFactory.getLogger(AppRootResource.class);

    @Inject
    HttpServletRequest request;

    @Inject
    OAuthClient oauthClient;

    @GET
    public Response ping() throws Exception {
        OAuthToken token = OAuthToken.find(request);

        if (token == null) {
            return Response.temporaryRedirect(oauthClient.getAuthUrl().toURI()).build();
        } else {
            String uri = request.getContextPath() + "/static/mobile.html";
            return Response.temporaryRedirect(URI.create(uri)).build();
        }
    }
}
