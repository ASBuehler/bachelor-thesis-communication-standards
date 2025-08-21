package kketest.protocol;

import kketest.model.DataObject;
import kketest.serializer.Serializer;
import kketest.serializer.SerializerFactory;
import kketest.utility.MessErgebnis;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;

public class CoapReceiver implements Receiver {
    private final int port;
    private final String format;
    private CoapServer server;

    public CoapReceiver(int port, String format) {
        this.port = port;
        this.format = format;
    }
    
    @Override
    public void listen() throws Exception {
        
        
        // 1. Lade die Standardkonfiguration (aus der .properties-Datei)
        NetworkConfig config = NetworkConfig.getStandard();
        
        // 2. Erhöhe das Limit für die maximale Nachrichtengröße
        config.set(NetworkConfig.Keys.MAX_RESOURCE_BODY_SIZE, 20 * 1024 * 1024);
        
        // 3. Erstelle den Server mit der angepassten Konfiguration
        this.server = new CoapServer(config, port);

        // Data Handler (bleibt unverändert)
        server.add(new CoapResource("data") {
            @Override
            public void handlePOST(CoapExchange exchange) {
                try {
                    byte[] requestBytes = exchange.getRequestPayload();
                    System.out.println("RECEIVED_PAYLOAD_SIZE_bytes=" + requestBytes.length);
                    Serializer serializer = SerializerFactory.getSerializer(format);
                    MessErgebnis<DataObject> desErgebnis = serializer.deserialize(requestBytes, DataObject.class);
                    System.out.println("DESERIALIZATION_TIME_ns=" + desErgebnis.zeitInNs);
                    exchange.respond(CHANGED, "OK");
                } catch (Exception e) { exchange.respond(BAD_REQUEST, "Error"); }
            }
        });

        // Shutdown Handler (bleibt unverändert)
        server.add(new CoapResource("shutdown") {
            @Override
            public void handleGET(CoapExchange exchange) {
                System.out.println("Shutdown command received. Exiting.");
                exchange.respond(CONTENT, "OK, shutting down.");
                new Thread(() -> {
                    server.stop();
                    System.exit(0);
                }).start();
            }
        });

        server.start();
        System.out.println("CoAP Receiver started on port " + port);
        
        while(true) {
            Thread.sleep(Long.MAX_VALUE);
        }
    }
    
    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }
}