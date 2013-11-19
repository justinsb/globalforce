package us.globalforce.guice;

import java.net.URI;
import java.net.URISyntaxException;

import javax.sql.DataSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.globalforce.model.HumanWorker;

import com.fathomdb.Configuration;
import com.fathomdb.jpa.impl.ResultSetMappers;
import com.fathomdb.jpa.impl.ResultSetMappersProvider;
import com.fathomdb.server.http.JettyWebServerBuilder;
import com.fathomdb.server.http.WebServerBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;
import com.jolbox.bonecp.BoneCPDataSource;

public class GfGuiceModule extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(GfGuiceModule.class);

    final Configuration configuration;

    public GfGuiceModule(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(WebServerBuilder.class).to(JettyWebServerBuilder.class);

        bind(Configuration.class).toInstance(configuration);

        DataSource ds = buildDataSource();
        bind(DataSource.class).toInstance(ds);

        bind(ResultSetMappers.class).toProvider(Providers.guicify(ResultSetMappersProvider.build(HumanWorker.class)));

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setMaxConnectionsPerHost(10);
        HttpClient client = new HttpClient(connectionManager);

        bind(HttpClient.class).toInstance(client);

    }

    private DataSource buildDataSource() {
        URI dbUri;
        try {
            dbUri = new URI(System.getenv("DATABASE_URL"));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot parse DATABASE_URL", e);
        }

        String jdbcUsername = dbUri.getUserInfo().split(":")[0];
        String jdbcPassword = dbUri.getUserInfo().split(":")[1];
        String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            log.warn("Unable to load PG driver", e);
        }
        BoneCPDataSource ds = new BoneCPDataSource();
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(jdbcUsername);
        ds.setPassword(jdbcPassword);

        ds.setMaxConnectionsPerPartition(200);
        ds.setPartitionCount(1);

        return ds;

    }

}
