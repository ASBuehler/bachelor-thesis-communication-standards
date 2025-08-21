package kketest.protocol;

import com.sun.net.httpserver.HttpServer;
import kketest.model.DataObject;
import kketest.serializer.Serializer;
import kketest.serializer.SerializerFactory;
import kketest.utility.MessErgebnis;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HttpReceiver implements Receiver {
    private final int port;
    private final String format;
    private HttpServer server;

    public HttpReceiver(int port, String format) {
        this.port = port;
        this.format = format;
    }

    @Override
    public void listen() throws Exception {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Data Handler
        server.createContext("/data", httpExchange -> {
            try {
                byte[] requestBytes = httpExchange.getRequestBody().readAllBytes();
                System.out.println("RECEIVED_PAYLOAD_SIZE_bytes=" + requestBytes.length);
                Serializer serializer = SerializerFactory.getSerializer(format);
                MessErgebnis<DataObject> desErgebnis = serializer.deserialize(requestBytes, DataObject.class);
                System.out.println("DESERIALIZATION_TIME_ns=" + desErgebnis.zeitInNs);
                String response = "OK";
                httpExchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = httpExchange.getResponseBody()) { os.write(response.getBytes()); }
            } catch (Exception e) {
                System.err.println("Error processing /data request: " + e.getMessage());
            }
        });

        // Shutdown Handler, der den gesamten Java-Prozess beendet
        server.createContext("/shutdown", httpExchange -> {
            System.out.println("Shutdown command received. Exiting.");
            String response = "OK, shutting down.";
            httpExchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = httpExchange.getResponseBody()) { os.write(response.getBytes()); }
            
            new Thread(() -> {
                server.stop(1);
                System.exit(0); 
            }).start();
        });

        server.setExecutor(Executors.newFixedThreadPool(2));
        server.start();
        System.out.println("HTTP Receiver started on port " + port + ". Running until shutdown command...");
        
        // Hält den Haupt-Thread am Leben, bis System.exit() aufgerufen wird
        while(true) {
            Thread.sleep(Long.MAX_VALUE);
        }
    }

    @Override
    public void stop() { 
        if (server != null) {
            server.stop(0);
        }
    }
}