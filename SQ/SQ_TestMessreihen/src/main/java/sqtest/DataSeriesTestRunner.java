package sqtest;

import com.sun.management.OperatingSystemMXBean;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.util.Random;
import java.util.Set;

public class DataSeriesTestRunner {

    private static final int WARMUP_RUNS = 5000;
    private static final int MEASUREMENT_RUNS = 10_000;
    private static final int DATA_POINTS_COUNT = 10_000; // Number of values in our array

    public static void main(String[] args) throws Exception {

        // =======================================================
        // Testobjekte erstellen
        // =======================================================
        DataSeries testDataSeries = createTestDataSeries();
        DataSeries problematicData = createProblematicDataSeries();

        // =======================================================
        // Beispieldateien der *tatsächlichen* Testobjekte generieren
        // =======================================================
        System.out.println("--- Generating Example Files ---");
        System.out.println("Generating files for the PERFORMANCE test object (" + DATA_POINTS_COUNT + " data points)...");
        generateExampleFile("performance", "JSON", new JsonSerializer(), testDataSeries, "json");
        generateExampleFile("performance", "XML", new XmlSerializer(), testDataSeries, "xml");
        generateExampleFile("performance", "YAML", new YamlSerializer(), testDataSeries, "yaml");
        generateExampleFile("performance", "TOML", new TomlSerializer(), testDataSeries, "toml");
        generateExampleFile("performance", "CSV", new CsvSerializer(), testDataSeries, "csv");
        generateExampleFile("performance", "INI", new IniSerializer(), testDataSeries, "ini");
        
        System.out.println("\nGenerating files for the STABILITY test object...");
        generateExampleFile("stability", "JSON", new JsonSerializer(), problematicData, "json");
        generateExampleFile("stability", "XML", new XmlSerializer(), problematicData, "xml");
        // ... weitere Formate für den Stabilitätstest können hier bei Bedarf hinzugefügt werden
        
        System.out.println("--------------------------------\n");

        // =======================================================
        // Stability Test for DataSeries
        // =======================================================
        System.out.println("\n\n--- DataSeries Stability Test Started ---");

        runStabilityTest("JSON", new JsonSerializer(), problematicData);
        runStabilityTest("XML", new XmlSerializer(), problematicData);
        runStabilityTest("MessagePack", new MsgPackSerializer(), problematicData);
        runStabilityTest("CBOR", new CborSerializer(), problematicData);
        runStabilityTest("YAML", new YamlSerializer(), problematicData);
        runStabilityTest("BSON", new BsonSerializer(), problematicData);
        runStabilityTest("TOML", new TomlSerializer(), problematicData);
        runStabilityTest("Avro", new AvroSerializer(), problematicData);
        runStabilityTest("Protobuf", new ProtobufSerializer(), problematicData);
        runStabilityTest("CSV", new CsvSerializer(), problematicData);
        runStabilityTest("INI", new IniSerializer(), problematicData);

        System.out.println("\n--- DataSeries Stability Test Finished ---");

        // =======================================================
        // Test for DataSeries
        // =======================================================

        System.out.println("--- DataSeries Test Started ---");
        System.out.println("Number of data points in array: " + DATA_POINTS_COUNT);
        System.out.println("Number of warm-up runs: " + WARMUP_RUNS);
        System.out.println("Number of measurement runs: " + MEASUREMENT_RUNS);
        System.out.println("------------------------------------------");

        runTest("JSON", testDataSeries, new JsonSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("XML", testDataSeries, new XmlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("MessagePack", testDataSeries, new MsgPackSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("CBOR", testDataSeries, new CborSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("YAML", testDataSeries, new YamlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("BSON", testDataSeries, new BsonSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("TOML", testDataSeries, new TomlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("Avro", testDataSeries, new AvroSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("Protobuf", testDataSeries, new ProtobufSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("CSV", testDataSeries, new CsvSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("INI", testDataSeries, new IniSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        // runTest("Thrift", testDataSeries, new ThriftSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        
        System.out.println("\n--- DataSeries Test Finished ---");

    }

    private static <T> void generateExampleFile(String testType, String formatName, Serializer serializer, T testObject, String fileExtension) {
        Set<String> textFormats = Set.of("JSON", "XML", "YAML", "TOML", "CSV", "INI");

        if (!textFormats.contains(formatName)) {
            return; // Binäre Formate stillschweigend überspringen
        }

        try {
            byte[] data = serializer.serialize(testObject);
            File outputDir = new File("output_examples");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            String fileName = String.format("DataSeries_%s_%s.%s", testType, formatName.toLowerCase(), fileExtension);
            try (FileOutputStream fos = new FileOutputStream(new File(outputDir, fileName))) {
                fos.write(data);
                System.out.println("  -> Created example file: " + fileName);
            }
        } catch (Exception e) {
            System.out.println("  -> FAILED to create example file for " + formatName + ": " + e.getMessage());
        }
    }


    private static DataSeries createTestDataSeries() {
        double[] values = new double[DATA_POINTS_COUNT];
        Random rand = new Random();
        for (int i = 0; i < DATA_POINTS_COUNT; i++) {
            values[i] = 15.0 + 10.0 * rand.nextDouble();
        }
        return new DataSeries(
            "Sensor-A42-Garden",
            System.currentTimeMillis(),
            "Celsius",
            values
        );
    }

    public static <T> void runTest(String formatName, T testObject, Serializer serializer, int warmupRuns, int measurementRuns) throws Exception {
        System.out.println("\n[" + formatName + "]");
        Class<T> clazz = (Class<T>) testObject.getClass();

        byte[] serializedData;
        try {
            serializedData = serializer.serialize(testObject);
            T deserializedObjectCheck = serializer.deserialize(serializedData, clazz);
            if (deserializedObjectCheck == null) {
                System.out.println("  -> ERROR: Deserialization returned null!");
                return;
            }
        } catch (Exception e) {
            System.out.println("  -> ERROR: Initial serialize/deserialize failed: " + e.getClass().getSimpleName());
            // e.printStackTrace(); // Zur Fehlersuche bei Bedarf aktivieren
            return;
        }

        long serializedSize = serializedData.length;

        // WARM-UP PHASE mit Fortschrittsanzeige
        System.out.print("  -> Running warm-up phase... ");
        for (int i = 0; i < warmupRuns; i++) {
            serializer.serialize(testObject);
            // Alle 1000 Durchläufe ein Lebenszeichen geben
            if ((i + 1) % 1000 == 0) {
                System.out.print("."); // Gibt einen Punkt aus
            }
        }
        System.out.println(" done."); // Zeilenumbruch am Ende

        // VORBEREITUNG DER MESSUNG
        long totalSerializationTime = 0;
        long totalDeserializationTime = 0;
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double startCpuTime = osBean.getProcessCpuTime();

        // MESS-PHASE mit Fortschrittsanzeige
        System.out.print("  -> Running measurement phase... ");
        for (int i = 0; i < measurementRuns; i++) {
            long start = System.nanoTime();
            byte[] data = serializer.serialize(testObject);
            long end = System.nanoTime();
            totalSerializationTime += (end - start);

            start = System.nanoTime();
            serializer.deserialize(data, clazz);
            end = System.nanoTime();
            totalDeserializationTime += (end - start);
            
            // Alle 1000 Durchläufe ein Lebenszeichen geben
            if ((i + 1) % 1000 == 0) {
                System.out.print(".");
            }
        }
        System.out.println(" done."); // Zeilenumbruch am Ende
        
        double endCpuTime = osBean.getProcessCpuTime();
        double totalCpuTimeInSeconds = (endCpuTime - startCpuTime) / 1_000_000_000.0;

        long avgSerializationTime = totalSerializationTime / measurementRuns;
        long avgDeserializationTime = totalDeserializationTime / measurementRuns;

        System.out.println("  Results:");
        System.out.println("    Size (Bytes): " + serializedSize);
        System.out.println("    Average Serialization Time: " + avgSerializationTime + " ns");
        System.out.println("    Average Deserialization Time: " + avgDeserializationTime + " ns");
        System.out.println("    Total CPU Time for " + measurementRuns + " runs: " + String.format("%.4f", totalCpuTimeInSeconds) + " s");
    }


    //=============================
    // # Stabilitätstest-Logik #
    //=============================

    private static DataSeries createProblematicDataSeries() {
        // Enthält spezielle double-Werte, die viele Formate herausfordern
        double[] problematicValues = new double[] {
            1.0,
            -1.0,
            0.0,
            Double.NaN,             // Not a Number
            Double.POSITIVE_INFINITY, // Positive Unendlichkeit
            Double.NEGATIVE_INFINITY, // Negative Unendlichkeit
            Double.MAX_VALUE,       // Größter double-Wert
            Double.MIN_VALUE        // Kleinster positiver double-Wert
        };

        return new DataSeries(
            "Sensor-XYZ-ErrorCase-\"Special'Chars\"", // Sensor-ID mit Sonderzeichen
            -1L,                                     // Negativer Zeitstempel
            null,                                    // Einheit ist null
            problematicValues
        );
    }

    public static void runStabilityTest(String formatName, Serializer serializer, DataSeries problematicObject) {
        System.out.println("\n[Stability Test for: " + formatName + "]");
        
        try {
            // 1. Versuche, das problematische Objekt zu serialisieren
            byte[] serializedData = serializer.serialize(problematicObject);
            System.out.println("  -> Serialization successful. Size: " + serializedData.length + " Bytes.");

            // 2. Versuche, die Daten wieder zu deserialisieren
            DataSeries deserializedObject = serializer.deserialize(serializedData, DataSeries.class);
            System.out.println("  -> Deserialization successful.");

            // 3. Überprüfe die Datenintegrität
            if (deserializedObject == null) {
                System.out.println("  -> Result: Not Robust. Deserialized object is null.");
                return;
            }

            // Überprüfe einige kritische Felder
            boolean sensorIdOk = problematicObject.getSensorId().equals(deserializedObject.getSensorId());
            boolean unitOk = (problematicObject.getUnit() == null && deserializedObject.getUnit() == null);
            
            // Spezielle Prüfung für das double-Array, da NaN und Infinity nicht mit == verglichen werden können
            boolean valuesOk = java.util.Arrays.equals(problematicObject.getValues(), deserializedObject.getValues());
            
            if (sensorIdOk && unitOk && valuesOk) {
                System.out.println("  -> Result: Robust. Data integrity maintained.");
            } else {
                System.out.println("  -> Result: Not Robust. Data was altered during serialization/deserialization.");
                if (!sensorIdOk) System.out.println("     - sensorId mismatch");
                if (!unitOk) System.out.println("     - unit mismatch (expected null)");
                if (!valuesOk) System.out.println("     - double[] values mismatch (NaN/Infinity issue?)");
            }

        } catch (Exception e) {
            // Wenn eine Exception auftritt, ist der Serializer nicht robust für diesen Fall.
            System.out.println("  -> Result: Failed. Process failed with Exception: " + e.getClass().getSimpleName());
            // e.printStackTrace(); // Zur Fehlersuche bei Bedarf aktivieren
        }
    }
}