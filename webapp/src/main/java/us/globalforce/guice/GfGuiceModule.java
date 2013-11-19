package us.globalforce.guice;

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
        String driverClassName = "org.postgresql.Driver";
        String jdbcUrl = configuration.get("jdbc.url");
        String jdbcUsername = configuration.get("jdbc.username");
        String jdbcPassword = configuration.get("jdbc.password");

        try {
            Class.forName(driverClassName);
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
