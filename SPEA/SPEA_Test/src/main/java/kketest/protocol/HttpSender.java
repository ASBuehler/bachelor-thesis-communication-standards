package kketest.protocol;

import kketest.utility.MessErgebnis;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpSender implements Sender {

    private final URL url;
    private final String contentType;

    public HttpSender(String host, int port, String format) throws Exception {
        this.url = new URL("http://" + host + ":" + port + "/data");
        if ("xml".equalsIgnoreCase(format)) this.contentType = "application/xml";
        else if ("protobuf".equalsIgnoreCase(format)) this.contentType = "application/x-protobuf";
        else if ("messagepack".equalsIgnoreCase(format)) this.contentType = "application/msgpack";
        else if ("cbor".equalsIgnoreCase(format)) this.contentType = "application/cbor";
        else this.contentType = "application/json";
    }

    @Override
    public MessErgebnis<Void> send(byte[] data) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestProperty("Content-Length", Integer.toString(data.length));

        long requestStartTime = System.nanoTime();
        try (OutputStream os = conn.getOutputStream()) {
            
            int chunkSize = 8192; // 8 KB
            int chunksBeforePause = 128; // Pausiere nach 128 Chunks (~1 MB)
            int chunkCounter = 0;

            for (int i = 0; i < data.length; i += chunkSize) {
                int length = Math.min(chunkSize, data.length - i);
                os.write(data, i, length);

                // Wende die Drosselung NUR bei Payloads über 100 KB an
                if (data.length > 100000) {
                    chunkCounter++;
                    if (chunkCounter >= chunksBeforePause) {
                        Thread.sleep(3); // 3 Millisekunden Pause
                        chunkCounter = 0; // Zähler zurücksetzen
                    }
                }
            }
        }
        
        int responseCode = conn.getResponseCode();
        long requestEndTime = System.nanoTime();

        System.out.println("HTTP_RESPONSE_CODE=" + responseCode);
        
        conn.disconnect();
        return new MessErgebnis<>(null, requestEndTime - requestStartTime);
    }
}