package us.globalforce.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import com.salesforce.client.oauth.OAuthToken;

public class OAuthTokenProvider implements Provider<OAuthToken> {
    @Inject
    HttpServletRequest request;

    @Override
    public OAuthToken get() {
        return OAuthToken.find(request);
    }

}
