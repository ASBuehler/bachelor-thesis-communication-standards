package kketest.utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class ProcessExecutor {

    // Führt einen Befehl aus und wartet nicht auf das Ergebnis (startet im Hintergrund)
    public static Process start(String... command) throws Exception {
        System.out.println("Executing start command: " + String.join(" ", command));
        ProcessBuilder pb = new ProcessBuilder(command);
        return pb.start();
    }

    // Führt einen Befehl aus und wartet, bis er beendet ist
    public static void executeAndWait(String... command) throws Exception {
        System.out.println("Executing wait command: " + String.join(" ", command));
        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        // Optional: Ausgabe des Prozesses auf der Konsole anzeigen
        new Thread(() -> new BufferedReader(new InputStreamReader(process.getInputStream())).lines().forEach(System.out::println)).start();
        new Thread(() -> new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().forEach(System.err::println)).start();

        process.waitFor(30, TimeUnit.SECONDS); // Wartet maximal 30 Sekunden
    }
}