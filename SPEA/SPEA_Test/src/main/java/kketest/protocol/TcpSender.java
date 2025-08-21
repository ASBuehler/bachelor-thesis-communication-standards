package kketest.protocol;

import kketest.utility.MessErgebnis;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpSender implements Sender {
    private final String host;
    private final int port;

    public TcpSender(String host, int port, String format) {
        this.host = host;
        this.port = port;
    }

    @Override
    public MessErgebnis<Void> send(byte[] data) throws Exception {
        long rtt = 0;
        try (Socket socket = new Socket(host, port)) {
            OutputStream output = socket.getOutputStream();
            InputStream input = socket.getInputStream();

            long requestStartTime = System.nanoTime();
            
            int chunkSize = 8192; // 8 KB
            int chunksBeforePause = 128; // Pausiere nach 128 Chunks (~1 MB)
            int chunkCounter = 0;

            for (int i = 0; i < data.length; i += chunkSize) {
                int length = Math.min(chunkSize, data.length - i);
                output.write(data, i, length);
                
                if (data.length > 100000) {
                    chunkCounter++;
                    if (chunkCounter >= chunksBeforePause) {
                        Thread.sleep(11); // 11 Millisekunden Pause
                        chunkCounter = 0;
                    }
                }
            }
            
            output.flush();
            socket.shutdownOutput(); 

            byte[] response = input.readAllBytes();
            long requestEndTime = System.nanoTime();
            rtt = requestEndTime - requestStartTime;

            System.out.println("TCP_RESPONSE=" + new String(response));
        }
        return new MessErgebnis<>(null, rtt);
    }
}