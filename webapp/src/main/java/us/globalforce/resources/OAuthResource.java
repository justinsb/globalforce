package us.globalforce.resources;

import java.net.URI;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.globalforce.model.Credential;
import us.globalforce.salesforce.client.oauth.OAuthClient;
import us.globalforce.salesforce.client.oauth.OAuthToken;
import us.globalforce.services.JdbcRepository;

import com.google.common.base.Strings;

@Path("/oauth")
public class OAuthResource {
    private static final Logger log = LoggerFactory.getLogger(OAuthResource.class);

    private static final long serialVersionUID = 1L;

    @Inject
    JdbcRepository repository;

    @Inject
    OAuthClient oauthClient;

    @Inject
    HttpServletRequest request;

    @GET
    public Response doGet() throws Exception {
        OAuthToken authToken = OAuthToken.find(request);
        // (String) request.getSession().getAttribute(ACCESS_TOKEN);

        if (authToken == null) {
            String code = request.getParameter("code");

            if (Strings.isNullOrEmpty(code)) {
                // we need to send the user to authorize
                return Response.temporaryRedirect(oauthClient.getAuthUrl().toURI()).build();
            }

            log.info("Auth successful - got callback");

            OAuthToken token = oauthClient.validate(code);

            // TODO: We could just set the token direct into the session!

            token.storeInSession(request);

            saveCredential(token);

            // // Set a session attribute so that other servlets can get the access
            // // token
            // request.getSession().setAttribute(ACCESS_TOKEN, token.getAccessToken());
            //
            // // We also get the instance URL from the OAuth response, so set it
            // // in the session too
            // request.getSession().setAttribute(INSTANCE_URL, token.getInstanceUrl());
        }

        return Response.temporaryRedirect(URI.create(request.getContextPath() + "/static/mobile.html")).build();
    }

    private void saveCredential(OAuthToken token) {
        Credential credential = new Credential();
        credential.organization = token.getOrganizationId();
        credential.userId = token.getUserId();
        credential.refreshToken = token.getRefreshToken();

        repository.insertCredential(credential);
    }
}