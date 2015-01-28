package tesler.will.bitcoin;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONObject;


public class EventClient {
    public static void main(String[] args) {
        URI uri = URI.create("wss://ws-feed.exchange.coinbase.com");
        SslContextFactory sslContextFactory = new SslContextFactory(true);
        WebSocketClient client = new WebSocketClient(sslContextFactory);
        try {
            // The socket that receives events
            EventSocket socket = new EventSocket();
            client.start();
            // Attempt Connect
            Future<Session> fut = client.connect(socket, uri);
            // Wait for Connect
            Session session = fut.get();

            JSONObject json = new JSONObject();
            json.put("type", "subscribe");
            json.put("product_id", "BTC-USD");

            // Send a message
            session.getRemote().sendString(json.toString());

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        client.stop();
                    } catch (Exception e) {
                        System.err.println(e.getLocalizedMessage());
                    }
                    session.close();
                }
            };

            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
            exec.schedule(r, 10, TimeUnit.SECONDS);

        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }
}