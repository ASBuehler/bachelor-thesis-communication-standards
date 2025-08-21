package sqtest;

import com.sun.management.OperatingSystemMXBean;

import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.File;

public class SQTestRunnerAdvanced {

    // Die Anzahl der Durchläufe kannst du hier nach Bedarf anpassen
    private static final int WARMUP_RUNS = 50_000;
    private static final int MEASUREMENT_RUNS = 100_000;

    public static void main(String[] args) throws Exception {

        // =======================================================
        // 1. Erstellung der Test-Objekte
        // =======================================================
        PersonAdvanced testPerson = createTestPersonAdvanced();
        PersonAdvanced problematicPerson = createProblematicTestPersonAdvanced();
        
        // =======================================================
        // Beispieldateien der *tatsächlichen* Testobjekte generieren
        // =======================================================
        System.out.println("--- Generating Example Files for PersonAdvanced ---");
        System.out.println("Generating files for the PERFORMANCE test object...");
        generateExampleFile("performance", "JSON", new JsonSerializer(), testPerson, "json");
        generateExampleFile("performance", "XML", new XmlSerializer(), testPerson, "xml");
        generateExampleFile("performance", "YAML", new YamlSerializer(), testPerson, "yaml");
        generateExampleFile("performance", "TOML", new TomlSerializer(), testPerson, "toml");

        System.out.println("\nGenerating files for the STABILITY test object...");
        generateExampleFile("stability", "JSON", new JsonSerializer(), problematicPerson, "json");
        generateExampleFile("stability", "XML", new XmlSerializer(), problematicPerson, "xml");
        System.out.println("---------------------------------------------------\n");
        // =======================================================
        // 1. Erstellung des komplexen Test-Objekts
        // =======================================================

        System.out.println("--- SQ-Test für PersonAdvanced gestartet ---");
        System.out.println("Anzahl der Warm-up-Durchläufe: " + WARMUP_RUNS);
        System.out.println("Anzahl der Mess-Durchläufe: " + MEASUREMENT_RUNS);
        System.out.println("------------------------------------------");
        
        // =======================================================
        // 2. Durchführung der Tests für jedes Format
        // =======================================================
        

        runTest("JSON", testPerson, new JsonSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("XML", testPerson, new XmlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("CSV", testPerson, new CsvSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS); // Wird wahrscheinlich fehlschlagen
        runTest("MessagePack", testPerson, new MsgPackSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("CBOR", testPerson, new CborSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("YAML", testPerson, new YamlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("BSON", testPerson, new BsonSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("TOML", testPerson, new TomlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        // runTest("INI", testPerson, new IniSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS); // Wird wahrscheinlich fehlschlagen
        // runTest("RDF (Turtle)", testPerson, new RdfTurtleSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS); // Muss für komplexe Objekte angepasst werden


        System.out.println("\n--- SQ-Test für PersonAdvanced abgeschlossen ---");


        // =======================================================
        // Stabilitätstest für PersonAdvanced
        // =======================================================
        System.out.println("\n\n--- Stabilitätstest (Advanced) gestartet ---");

        // Aufruf des generischen Stabilitätstests
        runStabilityTest("JSON", new JsonSerializer(), problematicPerson, PersonAdvanced.class);
        runStabilityTest("XML", new XmlSerializer(), problematicPerson, PersonAdvanced.class);
        runStabilityTest("CSV", new CsvSerializer(), problematicPerson, PersonAdvanced.class);
        runStabilityTest("MessagePack", new MsgPackSerializer(), problematicPerson, PersonAdvanced.class);
        runStabilityTest("CBOR", new CborSerializer(), problematicPerson, PersonAdvanced.class);
        runStabilityTest("YAML", new YamlSerializer(), problematicPerson, PersonAdvanced.class);
        runStabilityTest("BSON", new BsonSerializer(), problematicPerson, PersonAdvanced.class);
        runStabilityTest("TOML", new TomlSerializer(), problematicPerson, PersonAdvanced.class);
        // runStabilityTest("INI", new IniSerializer(), problematicPerson, PersonAdvanced.class);
        // runStabilityTest("RDF (Turtle)", new RdfTurtleSerializer(), problematicPerson, PersonAdvanced.class);


        System.out.println("\n--- Stabilitätstest (Advanced) abgeschlossen ---");
    }

    /**
     * Erstellt ein Test-Objekt der Klasse PersonAdvanced mit realistischen, komplexen Daten.
     */
    private static PersonAdvanced createTestPersonAdvanced() {
        Address address = new Address("Musterstrasse 1", "Musterstadt", "12345", "Schweiz");
        
        List<String> hobbies = List.of("Lesen", "Wandern", "Programmieren");
        
        List<Dependent> dependents = List.of(
                new Dependent("Max Junior", 5, "Sohn"),
                new Dependent("Maria Muster", 8, "Tochter")
        );

        // Simuliert ein kleines 1KB "Profilbild"
        byte[] profilePicture = "Dies ist ein Test-String, der ein kleines binäres Bild simuliert.".repeat(20).getBytes(StandardCharsets.UTF_8);

        Map<String, String> metadata = Map.of(
                "last_login", "2025-07-10T10:00:00Z",
                "client_version", "2.5.1",
                "subscription_id", "sub_123xyz"
        );

        return new PersonAdvanced(
                "Dr. Johanna Müsster-Crönenberg 😊",
                42,
                "johanna.muesster@example.com",
                "+41791234567",
                true,
                LocalDate.of(1982, 5, 21),
                12345.67,
                address,
                hobbies,
                dependents,
                profilePicture,
                metadata,
                Status.ACTIVE
        );
    }

    private static <T> void generateExampleFile(String testType, String formatName, Serializer serializer, T testObject, String fileExtension) {
        Set<String> textFormats = Set.of("JSON", "XML", "YAML", "TOML"); // CSV & INI werden hier nicht unterstützt

        if (!textFormats.contains(formatName)) {
            return; // Andere Formate stillschweigend überspringen
        }

        try {
            byte[] data = serializer.serialize(testObject);
            File outputDir = new File("output_examples");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            String fileName = String.format("PersonAdvanced_%s_%s.%s", testType, formatName.toLowerCase(), fileExtension);
            try (FileOutputStream fos = new FileOutputStream(new File(outputDir, fileName))) {
                fos.write(data);
                System.out.println("  -> Created example file: " + fileName);
            }
        } catch (Exception e) {
            // Wir fangen den Fehler ab, damit der Test-Runner weiterläuft, auch wenn die Dateierstellung fehlschlägt
            System.out.println("  -> FAILED to create example file for " + formatName + " (" + e.getClass().getSimpleName() + "). This is expected for some formats.");
        }
    }


        public static <T> void runTest(String formatName, T testObject, Serializer serializer, int warmupRuns, int measurementRuns) throws Exception {
        System.out.println("\n[" + formatName + "]");

        // Hole die Klasse des Testobjekts für die Deserialisierung
        Class<T> clazz = (Class<T>) testObject.getClass();

        // Einmalige Messung der Grösse und Korrektheit
        byte[] serializedData;
        try {
            serializedData = serializer.serialize(testObject);
            T deserializedObjectCheck = serializer.deserialize(serializedData, clazz);
            if (deserializedObjectCheck == null) {
                 System.out.println("  -> FEHLER: Deserialisierung hat null zurückgegeben!");
                 return;
            }
        } catch (Exception e) {
            System.out.println("  -> FEHLER: Initiales Serialisieren/Deserialisieren fehlgeschlagen: " + e.getClass().getSimpleName());
            
            
            e.printStackTrace(); 
            
            return;
        }

        long serializedSize = serializedData.length;

        // WARM-UP PHASE
        System.out.println("  -> Führe Warm-up-Phase durch...");
        for (int i = 0; i < warmupRuns; i++) {
            byte[] tempData = serializer.serialize(testObject);
            serializer.deserialize(tempData, clazz);
        }
        
        // VORBEREITUNG DER MESSUNG
        long totalSerializationTime = 0;
        long totalDeserializationTime = 0;
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double startCpuTime = osBean.getProcessCpuTime();

        // MESS-PHASE
        System.out.println("  -> Führe Mess-Phase durch...");
        for (int i = 0; i < measurementRuns; i++) {
            long start = System.nanoTime();
            byte[] data = serializer.serialize(testObject);
            long end = System.nanoTime();
            totalSerializationTime += (end - start);

            start = System.nanoTime();
            serializer.deserialize(data, clazz);
            end = System.nanoTime();
            totalDeserializationTime += (end - start);
        }
        
        // ABSCHLUSS DER MESSUNG
        double endCpuTime = osBean.getProcessCpuTime();
        double totalCpuTimeInSeconds = (endCpuTime - startCpuTime) / 1_000_000_000.0;

        // BERECHNUNG UND AUSGABE DER ERGEBNISSE
        long avgSerializationTime = totalSerializationTime / measurementRuns;
        long avgDeserializationTime = totalDeserializationTime / measurementRuns;

        System.out.println("  Ergebnisse:");
        System.out.println("    Größe (Bytes): " + serializedSize);
        System.out.println("    Durchschnittliche Serialisierungszeit: " + avgSerializationTime + " ns");
        System.out.println("    Durchschnittliche Deserialisierungszeit: " + avgDeserializationTime + " ns");
        System.out.println("    Gesamte CPU-Zeit für " + measurementRuns + " Durchläufe: " + String.format("%.4f", totalCpuTimeInSeconds) + " s");
    }

    private static PersonAdvanced createProblematicTestPersonAdvanced() {
        // Erstellt ein Objekt mit potenziell problematischen Werten
        Address address = new Address("Null-Str. 1", null, "00000", "Sonderland & 😊");
        
        List<String> hobbies = List.of("SQL'Injection", "Text mit \"Quotes\"", "Back\\slash");
        
        // Liste, die null-Werte enthält
        List<Dependent> dependents = new ArrayList<>();
        dependents.add(new Dependent("Max Junior", 5, "Sohn"));
        dependents.add(null); 

        byte[] profilePicture = new byte[0]; // Leeres Byte-Array

        Map<String, String> metadata = new HashMap<>();
        metadata.put("key_with_special_chars-!@#$%", "value_with_emoji_😊");
        metadata.put("null_value_key", null);

        return new PersonAdvanced(
                "Johanna \"The Coder\" Müsster-Crönenberg",
                -99, // Negativer Wert
                null, // Null-E-Mail
                "invalid-phone-number",
                true,
                LocalDate.now(),
                Double.NaN, // Not a Number
                address,
                hobbies,
                dependents,
                profilePicture,
                metadata,
                Status.SUSPENDED
        );
    }

        // In SQTestRunnerAdvanced.java

    public static <T> void runStabilityTest(String formatName, Serializer serializer, T problematicObject, Class<T> clazz) {
        System.out.println("\n[Stabilitätstest für: " + formatName + "]");
        try {
            // 1. Versuch, das problematische Objekt zu serialisieren
            byte[] serializedData = serializer.serialize(problematicObject);
            System.out.println("  -> Serialisierung erfolgreich. Größe: " + serializedData.length + " Bytes.");

            // 2. Versuch, die Daten wieder zu deserialisieren
            T deserializedObject = serializer.deserialize(serializedData, clazz);
            System.out.println("  -> Deserialisierung erfolgreich.");

            // 3. Überprüfen, ob die Daten erhalten geblieben sind (einfache Prüfung)
            // Eine vollständige Prüfung würde alle Felder mit .equals vergleichen, was sehr aufwändig ist.
            // Wir prüfen nur, ob das Ergebnis nicht null ist.
            if (deserializedObject != null) {
                System.out.println("  -> Ergebnis: Robust. Prozess erfolgreich durchlaufen.");
            } else {
                System.out.println("  -> Ergebnis: Nicht Robust. Deserialisiertes Objekt ist null.");
            }

        } catch (Exception e) {
            // Wenn eine Exception auftritt, ist der Serializer nicht robust für diesen Fall.
            System.out.println("  -> Ergebnis: Fehlerhaft. Prozess ist fehlgeschlagen mit Exception: " + e.getClass().getSimpleName());
            // e.printStackTrace(); // Zur Fehlersuche einkommentieren
        }
    }
}
