package kketest;

import kketest.model.DataObject;
import kketest.protocol.ProtocolFactory;
import kketest.protocol.Receiver;
import kketest.protocol.Sender;
import kketest.serializer.Serializer;
import kketest.serializer.SerializerFactory;
import kketest.utility.MessErgebnis;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    // Netzwerkkonstanten, die von den Protokoll-Implementierungen verwendet werden
    public static final String EMPFAENGER_IP = "192.168.100.20";
    public static final int HTTP_PORT = 8080;
    public static final int COAP_PORT = 5683; // Standard CoAP Port
    public static final int MQTT_PORT = 1883; // Standard MQTT Port
    public static final int TCP_PORT = 8082;
    public static final int UDP_PORT = 8083;
    public static final String SHARED_FOLDER_PATH = "/mnt/hgfs/shared";

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("FEHLER: Falsche Anzahl an Argumenten.");
            System.err.println("Verwendung: java -jar ... <sender|receiver> <payload_size> <format> <protocol> <qos_or_run_id>");
            System.exit(1);
        }

        String role = args[0];
        int payloadSize = Integer.parseInt(args[1]);
        String format = args[2];
        String protocol = args[3];
        String lastArg = args[4]; // Kann QoS oder Run ID sein

        int port;
        if ("coap".equalsIgnoreCase(protocol)) port = COAP_PORT;
        else if ("mqtt".equalsIgnoreCase(protocol)) port = MQTT_PORT;
        else if ("tcp".equalsIgnoreCase(protocol)) port = TCP_PORT;
        else if ("udp".equalsIgnoreCase(protocol)) port = UDP_PORT;
        else port = HTTP_PORT;

        if ("receiver".equalsIgnoreCase(role)) {
            int qos = "mqtt".equalsIgnoreCase(protocol) ? Integer.parseInt(lastArg) : 0;
            Receiver receiver = ProtocolFactory.createReceiver(protocol, port, format, qos);
            receiver.listen();
        } else if ("sender".equalsIgnoreCase(role)) {
            int qos = "mqtt".equalsIgnoreCase(protocol) ? Integer.parseInt(lastArg) : 0;
            Sender sender = ProtocolFactory.createSender(protocol, EMPFAENGER_IP, port, format, qos);
            startSender(sender, payloadSize, format, protocol, SHARED_FOLDER_PATH);
        } else {
            System.err.println("FEHLER: Ungültige Rolle.");
            System.exit(1);
        }
    }

    /**
     * Die zentrale Sende-Methode in der neuen Konfiguration.
     * Liest eine vorserialisierte Datei und sendet deren Inhalt.
     * Misst nicht mehr die Serialisierungszeit.
     *
     * @param sender Eine Implementierung des Sender-Interfaces.
     * @param targetSizeInBytes Die nominale Grösse der zu ladenden Datei.
     * @param format Das zu verwendende Datenformat (bestimmt die Dateiendung).
     * @param protocol Das zu verwendende Protokoll.
     * @param sharedFolderPath Der Pfad zum Ordner, der die Payload-Dateien enthält.
     */
    private static void startSender(Sender sender, int targetSizeInBytes, String format, String protocol, String sharedFolderPath) throws Exception {
        System.out.println("Sender gestartet. Protokoll: " + protocol + ", Format: " + format + ", Zieldatei: " + targetSizeInBytes + " Bytes");

        // Schritt 1: Dateinamen basierend auf den Parametern konstruieren
        // Da wir nur noch JSON-Dateien als Quelle verwenden, ist die Endung immer .json
        String fileName = "payload_" + targetSizeInBytes + "B.json";
        Path filePath = Paths.get(sharedFolderPath, "payloads", fileName);

        // Schritt 2: Datei einlesen
        if (!Files.exists(filePath)) {
            System.err.println("FEHLER: Payload-Datei nicht gefunden unter: " + filePath);
            // Wir senden einen Shutdown, damit der Empfänger nicht ewig wartet
            if ("http".equalsIgnoreCase(protocol)) sendHttpShutdownCommand();
            else if ("coap".equalsIgnoreCase(protocol)) sendCoapShutdownCommand();
            System.exit(1);
        }
        
        byte[] dataToSend = Files.readAllBytes(filePath);

        // Die Serialisierung entfällt. Wir geben die exakte Dateigrösse als Payload-Grösse aus.
        System.out.println("PAYLOAD_SIZE_bytes=" + dataToSend.length);
        
        // Schritt 3: Senden und RTT messen (die Logik dafür liegt in der Sender-Implementierung)
        MessErgebnis<Void> netErgebnis = sender.send(dataToSend);
        System.out.println("ROUND_TRIP_TIME_ns=" + netErgebnis.zeitInNs);
        
        System.out.println("Sender beendet.");
        
        // Schritt 4: Sende den protokoll-spezifischen Shutdown-Befehl
        if ("http".equalsIgnoreCase(protocol)) {
            sendHttpShutdownCommand();
        } else if ("coap".equalsIgnoreCase(protocol)) {
            sendCoapShutdownCommand();
        }
        // Für MQTT, TCP, UDP ist kein expliziter Shutdown-Befehl vom Sender nötig
    }

    /**
     * Sendet den Shutdown-Befehl über HTTP.
     */
    private static void sendHttpShutdownCommand() {
        try {
            System.out.println("Sende HTTP Shutdown-Befehl...");
            URL url = new URL("http://" + EMPFAENGER_IP + ":" + HTTP_PORT + "/shutdown");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();
            conn.disconnect();
            System.out.println("Shutdown-Befehl gesendet.");
        } catch (Exception e) { /* Ignoriere Fehler */ }
    }

    /**
     * Sendet den Shutdown-Befehl über CoAP.
     */
    private static void sendCoapShutdownCommand() {
        try {
            System.out.println("Sende CoAP Shutdown-Befehl...");
            // Wir verwenden hier direkt den CoapClient aus der CoapSender-Klasse,
            // um Code-Duplizierung zu vermeiden. In einer größeren Anwendung
            // würde man dies eleganter lösen.
            org.eclipse.californium.core.CoapClient client = new org.eclipse.californium.core.CoapClient("coap://" + EMPFAENGER_IP + ":" + COAP_PORT + "/shutdown");
            client.get();
            client.shutdown();
            System.out.println("Shutdown-Befehl gesendet.");
        } catch (Exception e) { /* Ignoriere Fehler */ }
    }
}