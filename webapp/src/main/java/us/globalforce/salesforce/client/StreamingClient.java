package us.globalforce.salesforce.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.client.ClientSessionChannel.MessageListener;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.ClientTransport;
import org.cometd.client.transport.LongPollingTransport;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.globalforce.salesforce.client.oauth.OAuthToken;

public class StreamingClient {

    private static final Logger log = LoggerFactory.getLogger(StreamingClient.class);

    // The channel to subscribe to. Same as the name of the PushTopic.
    // Be sure to create this topic before running this sample.
    private final String channel;

    private final OAuthToken token;

    private static final String STREAMING_ENDPOINT_URI = "/cometd/29.0";

    // The long poll duration.
    private static final int CONNECTION_TIMEOUT = 20 * 1000; // milliseconds
    private static final int READ_TIMEOUT = 120 * 1000; // milliseconds

    public StreamingClient(OAuthToken token, String key) {
        this.token = token;
        this.channel = "/topic/" + key;
    }

    public void start() throws IOException {
        log.info("Running streaming client....");

        final BayeuxClient client = makeClient(token);
        client.getChannel(Channel.META_HANDSHAKE).addListener(new ClientSessionChannel.MessageListener() {

            @Override
            public void onMessage(ClientSessionChannel channel, Message message) {

                log.debug("[CHANNEL:META_HANDSHAKE]: " + message);

                boolean success = message.isSuccessful();
                if (!success) {
                    String error = (String) message.get("error");
                    if (error != null) {
                        log.warn("Error during HANDSHAKE: " + error);
                        log.warn("Exiting...");
                        return;
                    }

                    Exception e = (Exception) message.get("exception");
                    if (e != null) {
                        log.warn("Exception during HANDSHAKE", e);
                        log.warn("Exiting...");
                        return;
                    }
                }
            }

        });

        client.getChannel(Channel.META_CONNECT).addListener(new ClientSessionChannel.MessageListener() {
            @Override
            public void onMessage(ClientSessionChannel channel, Message message) {

                log.debug("[CHANNEL:META_CONNECT]: " + message);

                boolean success = message.isSuccessful();
                if (!success) {
                    String error = (String) message.get("error");
                    if (error != null) {
                        log.warn("Error during CONNECT: " + error);
                        log.warn("Exiting...");
                        return;
                    }
                }
            }

        });

        client.getChannel(Channel.META_SUBSCRIBE).addListener(new ClientSessionChannel.MessageListener() {

            @Override
            public void onMessage(ClientSessionChannel channel, Message message) {

                log.debug("[CHANNEL:META_SUBSCRIBE]: " + message);
                boolean success = message.isSuccessful();
                if (!success) {
                    String error = (String) message.get("error");
                    if (error != null) {
                        log.warn("Error during SUBSCRIBE: " + error);
                        log.warn("Exiting...");
                        return;
                    }
                }
            }
        });

        client.handshake();
        log.debug("Waiting for handshake");

        boolean handshaken = client.waitFor(10 * 1000, BayeuxClient.State.CONNECTED);
        if (!handshaken) {
            log.warn("Failed to handshake: " + client);
            return;
        }

        log.debug("Subscribing for channel: " + channel);

        client.getChannel(channel).subscribe(new MessageListener() {
            @Override
            public void onMessage(ClientSessionChannel channel, Message message) {
                log.info("Received Message: " + message);
            }
        });
    }

    private static BayeuxClient makeClient(final OAuthToken token) throws IOException {
        HttpClient httpClient = new HttpClient();
        httpClient.setConnectTimeout(CONNECTION_TIMEOUT);
        httpClient.setTimeout(READ_TIMEOUT);
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new IOException("Error starting bayeux http client", e);
        }

        // String[] pair = SoapLoginUtil.login(httpClient, USER_NAME, PASSWORD);
        //
        // if (pair == null) {
        // System.exit(1);
        // }

        // assert pair.length == 2;
        // final String sessionid = pair[0];
        // String endpoint = pair[1];
        // System.out.println("Login successful!\nEndpoint: " + endpoint + "\nSessionid=" + sessionid);

        Map<String, Object> options = new HashMap<String, Object>();
        options.put(ClientTransport.TIMEOUT_OPTION, READ_TIMEOUT);
        LongPollingTransport transport = new LongPollingTransport(options, httpClient) {

            @Override
            protected void customize(ContentExchange exchange) {
                super.customize(exchange);
                exchange.addRequestHeader("Authorization", token.getHeader());
            }
        };

        BayeuxClient client = new BayeuxClient(salesforceStreamingEndpoint(token.getInstanceUrl()), transport);
        return client;
    }

    private static String salesforceStreamingEndpoint(URL instanceUrl) throws MalformedURLException {
        return new URL(instanceUrl.toString() + STREAMING_ENDPOINT_URI).toExternalForm();
    }

}
