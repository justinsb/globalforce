package us.globalforce;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import us.globalforce.resources.DemoREST;
import us.globalforce.resources.OAuthServlet;

public class Main {

	public static void main(String[] args) throws Exception {
		Server server = new Server(Integer.valueOf(System.getenv("PORT")));
		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(new ServletHolder(new DemoREST()), "/DemoREST");
		context.addServlet(new ServletHolder(new OAuthServlet()), "/oauth/*");
		context.addServlet(new ServletHolder(new OAuthServlet()), "/oauth");
		server.start();
		server.join();
	}
}
