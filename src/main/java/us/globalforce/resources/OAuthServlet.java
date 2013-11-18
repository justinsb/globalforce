package us.globalforce.resources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Servlet parameters
 */
@WebServlet(name = "oauth", urlPatterns = { "/oauth/*", "/oauth" }, initParams = {
//		// clientId is 'Consumer Key' in the Remote Access UI
//		@WebInitParam(name = "clientId", value = "3MVG9A2kN3Bn17ht1Sa_5M8pmOHZuFU98yx.VxDUG7qkW9pqUk7c9tX57iXvSAB1k9VSbECGOaB79S_Agel0d"),
//		// clientSecret is 'Consumer Secret' in the Remote Access UI
//		@WebInitParam(name = "clientSecret", value = "295020390184049994"),
//		// This must be identical to 'Callback URL' in the Remote Access UI
//		@WebInitParam(name = "redirectUri", value = "https://http://pacific-gorge-1278.herokuapp.com/RestTest/oauth/_callback"),
//		@WebInitParam(name = "environment", value = "https://login.salesforce.com"),
		})
public class OAuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
	private static final String INSTANCE_URL = "INSTANCE_URL";

	private String clientId = "3MVG9A2kN3Bn17ht1Sa_5M8pmOHZuFU98yx.VxDUG7qkW9pqUk7c9tX57iXvSAB1k9VSbECGOaB79S_Agel0d";
	private String clientSecret = "295020390184049994";
	private String redirectUri = "https://http://pacific-gorge-1278.herokuapp.com/RestTest/oauth/_callback";
	private String environment = "https://login.salesforce.com";
	private String authUrl = null;
	private String tokenUrl = null;

	public void init() throws ServletException {
//		clientId = this.getInitParameter("clientId");
//		clientSecret = this.getInitParameter("clientSecret");
//		redirectUri = this.getInitParameter("redirectUri");
//		environment = this.getInitParameter("environment");

		try {
			authUrl = environment
					+ "/services/oauth2/authorize?response_type=code&client_id="
					+ clientId + "&redirect_uri="
					+ URLEncoder.encode(redirectUri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ServletException(e);
		}

		tokenUrl = environment + "/services/oauth2/token";
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String accessToken = (String) request.getSession().getAttribute(
				ACCESS_TOKEN);

		if (accessToken == null) {
			String instanceUrl = null;

			if (request.getRequestURI().endsWith("oauth")) {
				// we need to send the user to authorize
				response.sendRedirect(authUrl);
				return;
			} else {
				System.out.println("Auth successful - got callback");

				String code = request.getParameter("code");

				HttpClient httpclient = new HttpClient();

				PostMethod post = new PostMethod(tokenUrl);
				post.addParameter("code", code);
				post.addParameter("grant_type", "authorization_code");
				post.addParameter("client_id", clientId);
				post.addParameter("client_secret", clientSecret);
				post.addParameter("redirect_uri", redirectUri);

				try {
					httpclient.executeMethod(post);

					JsonParser parser = new JsonParser();
					JsonObject authResponse = parser.parse(
							new InputStreamReader(post
									.getResponseBodyAsStream()))
							.getAsJsonObject();
					System.out.println("Auth response: "
							+ authResponse.toString());

					accessToken = authResponse.get("access_token")
							.getAsString();
					instanceUrl = authResponse.get("instance_url")
							.getAsString();

					System.out.println("Got access token: " + accessToken);
				} finally {
					post.releaseConnection();
				}
			}

			// Set a session attribute so that other servlets can get the access
			// token
			request.getSession().setAttribute(ACCESS_TOKEN, accessToken);

			// We also get the instance URL from the OAuth response, so set it
			// in the session too
			request.getSession().setAttribute(INSTANCE_URL, instanceUrl);
		}

		response.sendRedirect(request.getContextPath() + "/DemoREST");
	}
}