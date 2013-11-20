package us.globalforce.resources;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.salesforce.client.oauth.OAuthClient;
import com.salesforce.client.oauth.OAuthToken;

/**
 * Servlet parameters
 */
@Singleton
@WebServlet(name = "oauth", urlPatterns = { "/oauth" }, initParams = {
// // clientId is 'Consumer Key' in the Remote Access UI
// @WebInitParam(name = "clientId", value =
// "3MVG9A2kN3Bn17ht1Sa_5M8pmOHZuFU98yx.VxDUG7qkW9pqUk7c9tX57iXvSAB1k9VSbECGOaB79S_Agel0d"),
// // clientSecret is 'Consumer Secret' in the Remote Access UI
// @WebInitParam(name = "clientSecret", value = "295020390184049994"),
// // This must be identical to 'Callback URL' in the Remote Access UI
// @WebInitParam(name = "redirectUri", value =
// "https://http://pacific-gorge-1278.herokuapp.com/RestTest/oauth/_callback"),
// @WebInitParam(name = "environment", value = "https://login.salesforce.com"),
})
public class OAuthServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(OAuthServlet.class);

    private static final long serialVersionUID = 1L;

    // private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    // private static final String INSTANCE_URL = "INSTANCE_URL";

    @Inject
    OAuthClient oauthClient;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        OAuthToken authToken = OAuthToken.find(request);
        // (String) request.getSession().getAttribute(ACCESS_TOKEN);

        if (authToken == null) {
            String code = request.getParameter("code");

            if (Strings.isNullOrEmpty(code)) {
                // we need to send the user to authorize
                response.sendRedirect(oauthClient.getAuthUrl().toString());
                return;
            }

            log.info("Auth successful - got callback");

            OAuthToken token = oauthClient.validate(code);

            // TODO: We could just set the token direct into the session!

            token.set(request);

            // // Set a session attribute so that other servlets can get the access
            // // token
            // request.getSession().setAttribute(ACCESS_TOKEN, token.getAccessToken());
            //
            // // We also get the instance URL from the OAuth response, so set it
            // // in the session too
            // request.getSession().setAttribute(INSTANCE_URL, token.getInstanceUrl());
        }

        response.sendRedirect(request.getContextPath() + "/static/mobile.html");
    }
}