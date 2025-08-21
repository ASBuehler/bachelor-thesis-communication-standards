package kketest.protocol;

import kketest.model.DataObject;
import kketest.serializer.Serializer;
import kketest.serializer.SerializerFactory;
import kketest.utility.MessErgebnis;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class UdpReceiver implements Receiver {
    private final int port;
    private final String format;

    public UdpReceiver(int port, String format) {
        this.port = port;
        this.format = format;
    }

    @Override
    public void listen() throws Exception {
        // Erstellt einen DatagramSocket, der auf dem Port lauscht
        try (DatagramSocket socket = new DatagramSocket(port)) {
            // UDP-Pakete können fragmentiert werden. Wir brauchen einen Puffer,
            // der groß genug für unsere größte Nachricht ist (z.B. 10MB + etwas extra)
            byte[] receiveBuffer = new byte[11 * 1024 * 1024]; 

            System.out.println("UDP Receiver started on port " + port + ". Waiting for a single datagram...");
            
            // receive() ist ein blockierender Aufruf: Das Programm wartet hier, bis ein Paket ankommt
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            // Schneide den Puffer auf die tatsächliche Länge der empfangenen Daten zu
            byte[] requestBytes = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
            System.out.println("RECEIVED_PAYLOAD_SIZE_bytes=" + requestBytes.length);

            // Deserialisiere die Daten
            Serializer serializer = SerializerFactory.getSerializer(format);
            MessErgebnis<DataObject> desErgebnis = serializer.deserialize(requestBytes, DataObject.class);
            System.out.println("DESERIALIZATION_TIME_ns=" + desErgebnis.zeitInNs);
            
            // Sende eine einfache "OK"-Bestätigung zurück an den Absender
            InetAddress senderAddress = receivePacket.getAddress();
            int senderPort = receivePacket.getPort();
            byte[] responseBytes = "OK".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(responseBytes, responseBytes.length, senderAddress, senderPort);
            socket.send(sendPacket);
        }
        // Der try-with-resources Block schließt den Socket automatisch
        System.out.println("UDP Receiver finished.");
    }

    @Override
    public void stop() {} // Nicht benötigt
}