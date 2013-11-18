package us.globalforce;

import java.io.File;

import org.apache.catalina.startup.Tomcat;

import us.globalforce.resources.DemoREST;
import us.globalforce.resources.OAuthServlet;

public class Main {

	public static void main(String[] args) throws Exception {
		// Server server = new Server(Integer.valueOf(System.getenv("PORT")));
		// ServletContextHandler context = new ServletContextHandler(
		// ServletContextHandler.SESSIONS);
		// context.setContextPath("/");
		// server.setHandler(context);
		// context.addServlet(new ServletHolder(new DemoREST()), "/DemoREST");
		// context.addServlet(new ServletHolder(new OAuthServlet()),
		// "/oauth/*");
		// context.addServlet(new ServletHolder(new OAuthServlet()), "/oauth");
		// server.start();
		// server.join();

		String webappDirLocation = "src/main/webapp/";
		Tomcat tomcat = new Tomcat();

		// The port that we should run on can be set into an environment
		// variable
		// Look for that variable and default to 8080 if it isn't there.
		String webPort = System.getenv("PORT");
		if (webPort == null || webPort.isEmpty()) {
			webPort = "8080";
		}

		tomcat.setPort(Integer.valueOf(webPort));

		tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());
		System.out.println("configuring app with basedir: "
				+ new File("./" + webappDirLocation).getAbsolutePath());

		tomcat.start();
		tomcat.getServer().await();
	}
}
