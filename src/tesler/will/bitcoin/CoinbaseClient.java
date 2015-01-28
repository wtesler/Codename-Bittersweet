package tesler.will.bitcoin;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONObject;

public class CoinbaseClient {

    public static final String COINBASE_MARKET_URL = "wss://ws-feed.exchange.coinbase.com";

    public static void main(String[] args) {
        CoinbaseClient.start();
    }

    public static void start() {

        try {

            // This the access point to the Coinbase Market Data API.
            URI uri = URI.create(COINBASE_MARKET_URL);

            // Retrieving Market Data is NOT a strictly secure connection...
            // Currently: Trusting All.
            SslContextFactory sslContextFactory = new SslContextFactory(true);

            // Establishes a session between Coinbase and us.
            WebSocketClient client = new WebSocketClient(sslContextFactory);

            // The socket that receives events.
            // Defined inside of here are the important session callbacks.
            CoinbaseSocket socket = new CoinbaseSocket();

            // Prepare the Client
            try {
                client.start();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            // Trys to connect.
            // Future.class here lets us treat the connect() as an asynchronous
            // receive
            // (IRecv).
            Future<Session> fSession = client.connect(socket, uri);

            // Maybe do something here...

            // Wait for Connect
            Session session = fSession.get();

            // Construct the subscribe message as defined in the API.
            JSONObject json = new JSONObject();
            json.put("type", "subscribe");
            json.put("product_id", "BTC-USD");

            // Send the subscribe message to the server.
            session.getRemote().sendString(json.toString());

            // This will end the session when run.
            Runnable timout = getTimeoutRunnable(client, session);

            // End the session in 10 seconds (for testing purposes)
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
            exec.schedule(timout, 10, TimeUnit.SECONDS);

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace(System.err);
        }
    }

    private static Runnable getTimeoutRunnable(WebSocketClient client, Session session) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    // Close the session and stop the client.
                    session.close();
                    client.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}