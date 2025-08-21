package kketest.protocol;

import kketest.utility.MessErgebnis;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSender implements Sender {
    private final String host;
    private final int port;

    public UdpSender(String host, int port, String format) {
        this.host = host;
        this.port = port;
    }

    @Override
    public MessErgebnis<Void> send(byte[] data) throws Exception {
        long rtt = 0;
        try (DatagramSocket socket = new DatagramSocket()) {
            // Setze einen Timeout für die Antwort, falls das Paket verloren geht
            socket.setSoTimeout(5000); // 5 Sekunden Timeout

            InetAddress address = InetAddress.getByName(host);
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, port);
            
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            long requestStartTime = System.nanoTime();

            // Sende das Datenpaket
            socket.send(sendPacket);

            try {
                // Warte auf die "OK"-Antwort vom Empfänger
                socket.receive(receivePacket);
                long requestEndTime = System.nanoTime();
                rtt = requestEndTime - requestStartTime;

                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("UDP_RESPONSE=" + response);
            } catch (java.net.SocketTimeoutException e) {
                System.out.println("UDP_RESPONSE=TIMEOUT (Paket wahrscheinlich verloren)");
                rtt = -1; // Kennzeichne Timeout
            }
        }
        return new MessErgebnis<>(null, rtt);
    }
}