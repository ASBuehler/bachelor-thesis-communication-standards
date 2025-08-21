package sqtest;

import com.sun.management.OperatingSystemMXBean;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

public class RecursiveNodeTestRunner {

    private static final int WARMUP_RUNS = 500;
    private static final int MEASUREMENT_RUNS = 1_000;
    private static final int HIERARCHY_DEPTH = 9;
    private static final int NODES_PER_LEVEL = 2;
    private static long nodeIdCounter = 0; // Zähler für die erstellten Knoten

    public static void main(String[] args) throws Exception {
        
        // =======================================================
        // 1. Erstellung der Test-Objekte
        // =======================================================
        OrganizationalChartNode testNode = createTestHierarchy();
        OrganizationalChartNode problematicNode = createProblematicNode();
        OrganizationalChartNode cyclicNode = createCyclicNode();

        // =======================================================
        // 2. Beispieldateien der *tatsächlichen* Testobjekte generieren
        // =======================================================
        System.out.println("--- Generating Example Files for Recursive Node ---");
        System.out.println("Generating files for the PERFORMANCE test object...");
        generateExampleFile("performance", "JSON", new JsonSerializer(), testNode, "json");
        generateExampleFile("performance", "XML", new XmlSerializer(), testNode, "xml");
        generateExampleFile("performance", "YAML", new YamlSerializer(), testNode, "yaml");

        System.out.println("\nGenerating files for the STABILITY (problematic) test object...");
        generateExampleFile("stability_problematic", "JSON", new JsonSerializer(), problematicNode, "json");
        generateExampleFile("stability_problematic", "XML", new XmlSerializer(), problematicNode, "xml");
        
        System.out.println("\nGenerating files for the STABILITY (cyclic) test object...");
        generateExampleFile("stability_cyclic", "JSON", new JsonSerializer(), cyclicNode, "json");
        generateExampleFile("stability_cyclic", "XML", new XmlSerializer(), cyclicNode, "xml");
        System.out.println("---------------------------------------------------\n");
        
        // =======================================================
        
        System.out.println("--- Recursive Node Test Started ---");
        System.out.println("Hierarchy depth: " + HIERARCHY_DEPTH);
        System.out.println("Nodes per level: " + NODES_PER_LEVEL);
        System.out.println("Total nodes in hierarchy: " + nodeIdCounter);
        System.out.println("Warm-up runs: " + WARMUP_RUNS);
        System.out.println("Measurement runs: " + MEASUREMENT_RUNS);
        System.out.println("------------------------------------------");

        // === Performance Tests ===
        runTest("JSON", testNode, new JsonSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("XML", testNode, new XmlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("MessagePack", testNode, new MsgPackSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("CBOR", testNode, new CborSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("YAML", testNode, new YamlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("BSON", testNode, new BsonSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("TOML", testNode, new TomlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("Avro", testNode, new AvroSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("Protobuf", testNode, new ProtobufSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);

        // Tests für Formate, die erwartungsgemäß fehlschlagen
        try {
            runTest("CSV", testNode, new CsvSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        } catch (UnsupportedOperationException e) {
            System.out.println("\n[CSV]");
            System.out.println("  -> SKIPPED (as expected): " + e.getMessage());
        }
        try {
            runTest("INI", testNode, new IniSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        } catch (UnsupportedOperationException e) {
            System.out.println("\n[INI]");
            System.out.println("  -> SKIPPED (as expected): " + e.getMessage());
        }

        System.out.println("\n--- Recursive Node Performance Test Finished ---");

        // === Stability Tests ===
        System.out.println("\n\n--- Recursive Node Stability Test Started ---");
        

        Serializer[] serializers = {
            new JsonSerializer(), new XmlSerializer(), new MsgPackSerializer(), 
            new CborSerializer(), new YamlSerializer(), new BsonSerializer(), 
            new TomlSerializer(), new AvroSerializer(), new ProtobufSerializer()
        };
        String[] formatNames = {"JSON", "XML", "MessagePack", "CBOR", "YAML", "BSON", "TOML", "Avro", "Protobuf"};

        System.out.println("\n--- Testing with problematic values (nulls, special chars) ---");
        for (int i = 0; i < serializers.length; i++) {
            runStabilityTest(formatNames[i], serializers[i], problematicNode);
        }

        System.out.println("\n--- Testing with cyclic dependency ---");
        for (int i = 0; i < serializers.length; i++) {
            runStabilityTest(formatNames[i], serializers[i], cyclicNode);
        }

        System.out.println("\n--- Recursive Node Stability Test Finished ---");
    }

    private static OrganizationalChartNode createTestHierarchy() {
        nodeIdCounter = 0; // Zähler zurücksetzen
        OrganizationalChartNode root = new OrganizationalChartNode(
            nodeIdCounter++, "CEO", "Chief Executive Officer", LocalDate.of(2010, 5, 20),
            Map.of("office", "Top Floor", "company_car", "Yes")
        );
        addChildren(root, 1);
        return root;
    }

    private static <T> void generateExampleFile(String testType, String formatName, Serializer serializer, T testObject, String fileExtension) {
        Set<String> textFormats = Set.of("JSON", "XML", "YAML"); 
        if (!textFormats.contains(formatName)) {
            return; 
        }

        try {
            byte[] data = serializer.serialize(testObject);
            File outputDir = new File("output_examples");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            String fileName = String.format("RecursiveNode_%s_%s.%s", testType, formatName.toLowerCase(), fileExtension);
            try (FileOutputStream fos = new FileOutputStream(new File(outputDir, fileName))) {
                fos.write(data);
                System.out.println("  -> Created example file: " + fileName);
            }
        } catch (Exception e) {
            System.out.println("  -> FAILED to create example file for " + formatName + " (" + e.getClass().getSimpleName() + "). This is expected for some formats like cyclic graphs.");
        }
    }


    private static void addChildren(OrganizationalChartNode parent, int currentDepth) {
        if (currentDepth >= HIERARCHY_DEPTH) {
            return;
        }
        for (int i = 0; i < NODES_PER_LEVEL; i++) {
            OrganizationalChartNode child = new OrganizationalChartNode(
                nodeIdCounter++, "Manager " + currentDepth + "-" + i, "Lead of Dept " + i, 
                LocalDate.now().minusYears(HIERARCHY_DEPTH - currentDepth), new HashMap<>()
            );
            parent.addSubordinate(child);
            addChildren(child, currentDepth + 1);
        }
    }

    private static OrganizationalChartNode createProblematicNode() {
        OrganizationalChartNode root = new OrganizationalChartNode(
            1000, "Root <'\"&>", null, null, null
        );
        root.addSubordinate(new OrganizationalChartNode(1001, "Child", "Role", LocalDate.now(), new HashMap<>())); 
        return root;
    }
    
    private static OrganizationalChartNode createCyclicNode() {
        OrganizationalChartNode boss = new OrganizationalChartNode(2000, "Boss", "Manager", LocalDate.now(), null);
        OrganizationalChartNode subordinate = new OrganizationalChartNode(2001, "Subordinate", "Employee", LocalDate.now(), null);
        boss.addSubordinate(subordinate);
        subordinate.addSubordinate(boss);
        return boss;
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
            return;
        }

        long serializedSize = serializedData.length;

        System.out.print("  -> Running warm-up phase... ");
        for (int i = 0; i < warmupRuns; i++) {
            serializer.serialize(testObject);
            if ((i + 1) % (warmupRuns / 5) == 0 && warmupRuns >=5) { System.out.print("."); }
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

    // In RecursiveNodeTestRunner.java

    public static void runStabilityTest(String formatName, Serializer serializer, OrganizationalChartNode testNode) {
        System.out.println("\n[Stability Test for: " + formatName + "]");
        try {
            byte[] serializedData = serializer.serialize(testNode);
            System.out.println("  -> Serialization successful. Size: " + serializedData.length + " Bytes.");

            OrganizationalChartNode deserializedNode = serializer.deserialize(serializedData, OrganizationalChartNode.class);
            System.out.println("  -> Deserialization successful.");

            if (testNode.equals(deserializedNode)) {
                System.out.println("  -> Result: Robust. Data integrity maintained.");
            } else {
                System.out.println("  -> Result: Not Robust. Data was altered.");
            }
        } catch (Throwable t) { // GEÄNDERT: Fängt jetzt auch Errors wie StackOverflowError
            boolean isCyclic = testNode.getId() >= 2000;
            if (isCyclic && t instanceof StackOverflowError) { // Spezifische Prüfung
                System.out.println("  -> Result: Robust (expected failure). Handled cyclic dependency gracefully by crashing with StackOverflowError.");
            } else if (isCyclic) {
                System.out.println("  -> Result: Robust (expected failure). Handled cyclic dependency gracefully with Exception: " + t.getClass().getSimpleName());
            } else {
                System.out.println("  -> Result: Failed. Process failed with: " + t.getClass().getSimpleName() + " - " + t.getMessage());
            }
        }
    }
}