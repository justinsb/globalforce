package us.globalforce;

import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

import us.globalforce.guice.GfServletModule;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;

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

		// The port that we should run on can be set into an environment
		// variable
		// Look for that variable and default to 8080 if it isn't there.
		String webPort = System.getenv("PORT");
		if (webPort == null || webPort.isEmpty()) {
			webPort = "8080";
		}

		List<Module> modules = Lists.newArrayList();
		modules.add(new GfServletModule());
		Injector injector = Guice.createInjector(modules);

		Server server = new Server(Integer.valueOf(webPort));
		ServletContextHandler root = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		root.setContextPath("/"); // technically not required, as "/" is the
									// default

		root.addFilter(GuiceFilter.class, "/*",
				EnumSet.of(DispatcherType.REQUEST));
		root.addServlet(DefaultServlet.class, "/");

		server.setHandler(root);

		server.start();

		while (true) {
			Thread.sleep(5000);
		}

		// String webappDirLocation = "src/main/webapp/";
		// Tomcat tomcat = new Tomcat();
		//

		// tomcat.setPort(Integer.valueOf(webPort));
		//
		// tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());
		// System.out.println("configuring app with basedir: "
		// + new File("./" + webappDirLocation).getAbsolutePath());
		//
		// tomcat.start();
		// tomcat.getServer().await();
	}
}
