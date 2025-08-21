package kketest.protocol;

import kketest.model.DataObject;
import kketest.serializer.Serializer;
import kketest.serializer.SerializerFactory;
import kketest.utility.MessErgebnis;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpReceiver implements Receiver {
    private final int port;
    private final String format;

    public TcpReceiver(int port, String format) {
        this.port = port;
        this.format = format;
    }

    @Override
    public void listen() throws Exception {
        // Erstellt einen Server-Socket, der auf dem Port lauscht
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCP Receiver started on port " + port + ". Waiting for a single connection...");
            
            // accept() ist ein blockierender Aufruf: Das Programm wartet hier, bis ein Client sich verbindet
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected.");

            // Lese die Daten aus dem eingehenden Stream
            InputStream input = clientSocket.getInputStream();
            byte[] requestBytes = input.readAllBytes();
            System.out.println("RECEIVED_PAYLOAD_SIZE_bytes=" + requestBytes.length);

            // Deserialisiere die Daten
            Serializer serializer = SerializerFactory.getSerializer(format);
            MessErgebnis<DataObject> desErgebnis = serializer.deserialize(requestBytes, DataObject.class);
            System.out.println("DESERIALIZATION_TIME_ns=" + desErgebnis.zeitInNs);

            // Sende eine einfache Bestätigung zurück, damit der Sender seine RTT messen kann
            OutputStream output = clientSocket.getOutputStream();
            output.write("OK".getBytes());
            output.flush();

            // Schließe die Verbindung und den Server
            clientSocket.close();
        }
        // Der try-with-resources Block schließt den ServerSocket automatisch
        System.out.println("TCP Receiver finished.");
    }

    @Override
    public void stop() {} // Nicht mehr benötigt, da listen() nach einer Verbindung endet
}