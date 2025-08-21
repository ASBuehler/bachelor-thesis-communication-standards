package sqtest;

import com.sun.management.OperatingSystemMXBean;

import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.io.File;
import java.util.function.BiPredicate;


public class InvoiceTestRunner {

    private static final int WARMUP_RUNS = 1_00;
    private static final int MEASUREMENT_RUNS = 2_000;
    private static final int LINE_ITEMS_COUNT = 15;

    public static void main(String[] args) throws Exception {

        // =======================================================
        // 1. Erstellung der Test-Objekte
        // =======================================================
        Invoice testInvoice = createTestInvoice();
        Invoice problematicInvoice = createProblematicInvoice();

        // =======================================================
        // 2. Beispieldateien der *tatsächlichen* Testobjekte generieren
        // =======================================================
        System.out.println("--- Generating Example Files for Invoice ---");
        System.out.println("Generating files for the PERFORMANCE test object...");
        generateExampleFile("performance", "JSON", new JsonSerializer(), testInvoice, "json");
        generateExampleFile("performance", "XML", new XmlSerializer(), testInvoice, "xml");
        generateExampleFile("performance", "YAML", new YamlSerializer(), testInvoice, "yaml");
        generateExampleFile("performance", "TOML", new TomlSerializer(), testInvoice, "toml");
        generateExampleFile("performance", "CSV", new CsvSerializer(), testInvoice, "csv");
        generateExampleFile("performance", "INI", new IniSerializer(), testInvoice, "ini");

        System.out.println("\nGenerating files for the STABILITY test object...");
        generateExampleFile("stability", "JSON", new JsonSerializer(), problematicInvoice, "json");
        generateExampleFile("stability", "XML", new XmlSerializer(), problematicInvoice, "xml");
        System.out.println("--------------------------------------------\n");
        
        System.out.println("--- Invoice Document Test Started ---");
        System.out.println("Number of line items per invoice: " + LINE_ITEMS_COUNT);
        System.out.println("Warm-up runs: " + WARMUP_RUNS);
        System.out.println("Measurement runs: " + MEASUREMENT_RUNS);
        System.out.println("------------------------------------------");
        
        // === Performance Tests ===
        runTest("JSON", testInvoice, new JsonSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("XML", testInvoice, new XmlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("MessagePack", testInvoice, new MsgPackSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("CBOR", testInvoice, new CborSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("YAML", testInvoice, new YamlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("BSON", testInvoice, new BsonSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("TOML", testInvoice, new TomlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("Avro", testInvoice, new AvroSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("Protobuf", testInvoice, new ProtobufSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);

        System.out.println("\n--- Invoice Document Performance Test Finished ---");

        // === Stability Tests ===
        System.out.println("\n\n--- Invoice Document Stability Test Started ---");
        
        runStabilityTest("JSON", new JsonSerializer(), problematicInvoice);
        runStabilityTest("XML", new XmlSerializer(), problematicInvoice);
        runStabilityTest("MessagePack", new MsgPackSerializer(), problematicInvoice);
        runStabilityTest("CBOR", new CborSerializer(), problematicInvoice);
        runStabilityTest("YAML", new YamlSerializer(), problematicInvoice);
        runStabilityTest("BSON", new BsonSerializer(), problematicInvoice);
        runStabilityTest("TOML", new TomlSerializer(), problematicInvoice);
        runStabilityTest("Avro", new AvroSerializer(), problematicInvoice);
        runStabilityTest("Protobuf", new ProtobufSerializer(), problematicInvoice);
        
        System.out.println("\n--- Invoice Document Stability Test Finished ---");
    }

    private static <T> void generateExampleFile(String testType, String formatName, Serializer serializer, T testObject, String fileExtension) {
        Set<String> textFormats = Set.of("JSON", "XML", "YAML", "TOML", "CSV", "INI");

        if (!textFormats.contains(formatName)) {
            return;
        }

        try {
            byte[] data = serializer.serialize(testObject);
            File outputDir = new File("output_examples");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            String fileName = String.format("Invoice_%s_%s.%s", testType, formatName.toLowerCase(), fileExtension);
            try (FileOutputStream fos = new FileOutputStream(new File(outputDir, fileName))) {
                fos.write(data);
                System.out.println("  -> Created example file: " + fileName);
            }
        } catch (Exception e) {
            System.out.println("  -> FAILED to create example file for " + formatName + ": " + e.getMessage());
        }
    }

    private static Invoice createTestInvoice() {
        Address address = new Address("Musterstrasse 123", "Musterstadt", "12345", "Deutschland");
        
        List<InvoiceLineItem> items = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < LINE_ITEMS_COUNT; i++) {
            items.add(new InvoiceLineItem(
                "PROD-" + (1000 + i),
                "Produkt " + (i + 1) + " mit einer Beschreibung.",
                1 + rand.nextInt(10),
                new BigDecimal(String.format("%.2f", 10.0 + rand.nextDouble() * 100).replace(',', '.'))
            ));
        }

        byte[] logo = new byte[1024]; // 1 KB Dummy-Logo
        rand.nextBytes(logo);

        String notes = "Vielen Dank für Ihren Einkauf.\nBitte überweisen Sie den Betrag innerhalb von 14 Tagen.\nSonderzeichen: äöüß'\"<>&";

        return new Invoice("INV-2025-001", LocalDate.now(), LocalDate.now().plusDays(14), address, items, new BigDecimal("0.19"), notes, logo);
    }

    private static Invoice createProblematicInvoice() {
        Invoice invoice = new Invoice(); // WICHTIG: Standardkonstruktor verwenden!
        
        // Setze die problematischen Werte manuell
        invoice.setInvoiceNumber(null);
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(null);
        invoice.setBillingAddress(new Address("Test Str", null, "12345", null));
        invoice.setLineItems(new ArrayList<>()); // Leere Liste
        
        // Hier sind subtotal, tax und total jetzt auch null, was dem deserialisierten Zustand entspricht
        invoice.setSubtotal(null);
        invoice.setTax(null);
        invoice.setTotal(null);

        invoice.setStatus(null);
        invoice.setNotes("Notes with\nLine Breaks and € Symbol.");
        invoice.setCompanyLogo(new byte[0]); // Leeres byte array
        
        return invoice;
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
            System.out.println("  -> ERROR: Initial serialize/deserialize failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            //e.printStackTrace(); // Zur Fehlersuche bei Bedarf aktivieren
            return;
        }

        long serializedSize = serializedData.length;

        System.out.print("  -> Running warm-up phase... ");
        for (int i = 0; i < warmupRuns; i++) {
            serializer.serialize(testObject);
            if ((i + 1) % (warmupRuns / 5) == 0 && warmupRuns >= 5) { System.out.print("."); }
        }
        System.out.println(" done.");

        long totalSerializationTime = 0;
        long totalDeserializationTime = 0;
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double startCpuTime = osBean.getProcessCpuTime();

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
            
            if ((i + 1) % (measurementRuns / 10) == 0 && measurementRuns >= 10) { System.out.print("."); }
        }
        System.out.println(" done.");
        
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

    public static <T> void runStabilityTest(String formatName, Serializer serializer, T testObject) {
        System.out.println("\n[Stability Test for: " + formatName + "]");
        try {
            byte[] serializedData = serializer.serialize(testObject);
            System.out.println("  -> Serialization successful. Size: " + serializedData.length + " Bytes.");

            T deserializedObject = serializer.deserialize(serializedData, (Class<T>) testObject.getClass());
            System.out.println("  -> Deserialization successful.");

            if (testObject.equals(deserializedObject)) {
                System.out.println("  -> Result: Robust. Data integrity maintained.");
            } else {
                System.out.println("  -> Result: Not Robust. Data was altered during serialization/deserialization.");
            }
        } catch (Exception e) {
            System.out.println("  -> Result: Failed. Process failed with Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            //e.printStackTrace();
        }
    }
}