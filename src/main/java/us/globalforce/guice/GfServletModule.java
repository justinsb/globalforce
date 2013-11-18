package us.globalforce.guice;

import us.globalforce.resources.DemoREST;
import us.globalforce.resources.OAuthServlet;

import com.google.inject.servlet.ServletModule;

public class GfServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		serve("/DemoREST").with(DemoREST.class);
		serve("/oauth").with(OAuthServlet.class);
		serve("/oauth/*").with(OAuthServlet.class);
	}
}
