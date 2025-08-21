package kketest.protocol;

import kketest.utility.MessErgebnis;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.net.URI;

public class CoapSender implements Sender {

    private final CoapClient client;
    private final int contentType;
    private static final int APPLICATION_MSGPACK_CUSTOM_ID = 11542;

    public CoapSender(String host, int port, String format) throws Exception {
        URI uri = new URI("coap://" + host + ":" + port + "/data");
        this.client = new CoapClient(uri);
        
        // Mappen des Format-Strings auf CoAP Content-Type Codes
        if ("xml".equalsIgnoreCase(format)) this.contentType = MediaTypeRegistry.APPLICATION_XML;
        else if ("protobuf".equalsIgnoreCase(format)) this.contentType = MediaTypeRegistry.APPLICATION_OCTET_STREAM; // Standard für binäre Daten
        else if ("messagepack".equalsIgnoreCase(format)) this.contentType = APPLICATION_MSGPACK_CUSTOM_ID;
        else if ("cbor".equalsIgnoreCase(format)) this.contentType = MediaTypeRegistry.APPLICATION_CBOR;
        else this.contentType = MediaTypeRegistry.APPLICATION_JSON;
    }

    @Override
    public MessErgebnis<Void> send(byte[] data) throws Exception {
        long requestStartTime = System.nanoTime();
        CoapResponse response = client.post(data, contentType);
        long requestEndTime = System.nanoTime();
        
        if (response != null) {
            System.out.println("COAP_RESPONSE_CODE=" + response.getCode());
        } else {
            System.out.println("COAP_RESPONSE_CODE=TIMEOUT");
        }
        
        client.shutdown();
        return new MessErgebnis<>(null, requestEndTime - requestStartTime);
    }
}