package kketest;

import java.net.Socket;
import java.net.InetSocketAddress;

public class NetworkDebug {
    public static void main(String[] args) {
        String host = "192.168.100.20";
        int port = 1883;
        int timeout = 5000; // 5 Sekunden

        System.out.println("Versuche, eine TCP-Verbindung zu " + host + ":" + port + " aufzubauen...");

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            System.out.println("******************************************");
            System.out.println("**** ERFOLG! Verbindung hergestellt. ****");
            System.out.println("******************************************");
            System.out.println("Das Problem liegt in der Paho MQTT-Bibliothek.");
        } catch (Exception e) {
            System.out.println("********************************************");
            System.out.println("**** FEHLER! Verbindung fehlgeschlagen. ****");
            System.out.println("********************************************");
            System.out.println("Das Problem liegt tief im Java Netzwerk-Stack oder der VM-Konfiguration.");
            e.printStackTrace();
        }
    }
}