package us.globalforce;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.jetty.server.Server;
import org.platformlayer.metrics.MetricReporter;
import org.platformlayer.metrics.NullMetricsModule;

import us.globalforce.guice.GfGuiceModule;
import us.globalforce.guice.GfGuiceServletModule;

import com.fathomdb.Configuration;
import com.fathomdb.config.ConfigurationImpl;
import com.fathomdb.jdbc.JdbcGuiceModule;
import com.fathomdb.server.http.WebServerBuilder;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class GlobalForceServer {
    static final int DEFAULT_PORT = 8080;

    @Inject
    WebServerBuilder serverBuilder;

    @Inject
    Injector injector;

    // @Inject
    // EncryptionStore encryptionStore;

    @Inject
    Configuration config;

    @Inject
    Configuration configuration;

    @Inject
    MetricReporter metricReporter;

    private Server jettyServer;

    public static void main(String[] args) throws Exception {
        // LogbackHook.attachToRootLogger();

        Configuration configuration = ConfigurationImpl.load();

        List<Module> modules = Lists.newArrayList();

        modules.add(new NullMetricsModule());
        // modules.add(new ConfigurationModule());
        // modules.add(new CacheModule());

        modules.add(new JdbcGuiceModule());
        modules.add(new GfGuiceModule(configuration));
        modules.add(new GfGuiceServletModule());

        Injector injector = Guice.createInjector(modules);

        GlobalForceServer server = injector.getInstance(GlobalForceServer.class);
        server.start();
    }

    public void start() throws Exception {
        // The port that we should run on can be set into an environment
        // variable
        // Look for that variable and default to 8080 if it isn't there.
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        // int port = configuration.lookup("listen.http.port", DEFAULT_PORT);

        int port = Integer.valueOf(webPort);

        // EnumSet<SslOption> options = EnumSet.noneOf(SslOption.class);
        // serverBuilder.addHttpsConnector(port, options);

        serverBuilder.enableSessions();
        serverBuilder.enableRequestLogging();

        serverBuilder.addHttpConnector(port, true);

        serverBuilder.addGuiceContext("/", injector);

        this.jettyServer = serverBuilder.start();

        metricReporter.start();

        // if (configuration.lookup("crawler.enabled", true)) {
        // jobScheduler.start();
        // }
    }

    public void stop() throws Exception {
        if (jettyServer != null) {
            jettyServer.stop();
        }
    }
}
