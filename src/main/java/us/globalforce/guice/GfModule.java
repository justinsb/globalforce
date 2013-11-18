package us.globalforce.guice;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import com.google.inject.AbstractModule;

public class GfModule extends AbstractModule {

    @Override
    protected void configure() {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.setMaxConnectionsPerHost(10);
        HttpClient client = new HttpClient(connectionManager);

        bind(HttpClient.class).toInstance(client);
    }

}
