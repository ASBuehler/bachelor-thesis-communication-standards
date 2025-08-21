package sqtest;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class SQTestRunner {

    // 1. Konfigurierbare Anzahl der Durchläufe
    //Warm-up und Messung getrennt definiert.
    private static final int WARMUP_RUNS = 5_000;
    private static final int MEASUREMENT_RUNS = 10_000;

    public static void main(String[] args) throws Exception {
        Person testPerson = new Person("Max Muster", 35, "max.muster@example.com");

        System.out.println("--- SQ-Test gestartet ---");
        System.out.println("Anzahl der Warm-up-Durchläufe: " + WARMUP_RUNS);
        System.out.println("Anzahl der Mess-Durchläufe: " + MEASUREMENT_RUNS);
        System.out.println("---------------------------------");

        // Wir übergeben die Anzahl der Durchläufe an die Testmethode
        runTest("JSON", testPerson, new JsonSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("XML", testPerson, new XmlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("CSV", testPerson, new CsvSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("MessagePack", testPerson, new MsgPackSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("CBOR", testPerson, new CborSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        runTest("YAML", testPerson, new YamlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);
        
        // Test für Avro
        System.out.println("\n[Avro]");
        AvroSerializer avroSerializer = new AvroSerializer();
        long totalAvroSerializationTime = 0;
        long totalAvroDeserializationTime = 0;
        byte[] avroData = avroSerializer.serialize(testPerson);

        // Warm-up für Avro
        System.out.println("  -> Führe Warm-up-Phase durch...");
        for (int i = 0; i < WARMUP_RUNS; i++) {
            avroSerializer.serialize(testPerson);
            avroSerializer.deserialize(avroData);
        }
        
        OperatingSystemMXBean osBeanAvro = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double startCpuTimeAvro = osBeanAvro.getProcessCpuTime();

        // Messung für Avro
        System.out.println("  -> Führe Mess-Phase durch...");
        for (int i = 0; i < MEASUREMENT_RUNS; i++) {
            long start = System.nanoTime();
            byte[] data = avroSerializer.serialize(testPerson);
            long end = System.nanoTime();
            totalAvroSerializationTime += (end - start);

            start = System.nanoTime();
            avroSerializer.deserialize(data);
            end = System.nanoTime();
            totalAvroDeserializationTime += (end - start);
        }

        // CPU-Messung für Avro abschliessen
        double endCpuTimeAvro = osBeanAvro.getProcessCpuTime();
        double totalCpuTimeAvroInSeconds = (endCpuTimeAvro - startCpuTimeAvro) / 1_000_000_000.0;

        System.out.println("  Ergebnisse:");
        System.out.println("    Größe (Bytes): " + avroData.length);
        System.out.println("    Durchschnittliche Serialisierungszeit: " + (totalAvroSerializationTime / MEASUREMENT_RUNS) + " ns");
        System.out.println("    Durchschnittliche Deserialisierungszeit: " + (totalAvroDeserializationTime / MEASUREMENT_RUNS) + " ns");
        // Ausgabe der CPU-Zeit für Avro
        System.out.println("    Gesamte CPU-Zeit für " + MEASUREMENT_RUNS + " Durchläufe: " + String.format("%.4f", totalCpuTimeAvroInSeconds) + " s");
        // Ende Test für Avro


        // Test für Thrift
        System.out.println("\n[Thrift]");
        ThriftSerializer thriftSerializer = new ThriftSerializer();
        long totalThriftSerializationTime = 0;
        long totalThriftDeserializationTime = 0;
        byte[] thriftData = thriftSerializer.serialize(testPerson);

        // Korrektheitstest für Thrift
        sqtest.Person deserializedThriftPersonCheck = thriftSerializer.deserialize(thriftData);
        if (deserializedThriftPersonCheck == null || !deserializedThriftPersonCheck.getName().equals(testPerson.getName())) {
            System.out.println("FEHLER: Deserialisierung für Thrift fehlgeschlagen!");
        } else {
            // Warm-up für Thrift
            System.out.println("  -> Führe Warm-up-Phase durch...");
            for (int i = 0; i < WARMUP_RUNS; i++) {
                thriftSerializer.serialize(testPerson);
                thriftSerializer.deserialize(thriftData);
            }

            // Vorbereitung der CPU-Messung für Thrift
            OperatingSystemMXBean osBeanThrift = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double startCpuTimeThrift = osBeanThrift.getProcessCpuTime();

            // Messung für Thrift
            System.out.println("  -> Führe Mess-Phase durch...");
            for (int i = 0; i < MEASUREMENT_RUNS; i++) {
                long start = System.nanoTime();
                byte[] data = thriftSerializer.serialize(testPerson);
                long end = System.nanoTime();
                totalThriftSerializationTime += (end - start);

                start = System.nanoTime();
                thriftSerializer.deserialize(data);
                end = System.nanoTime();
                totalThriftDeserializationTime += (end - start);
            }
            
            // CPU-Messung für Thrift abschliessen
            double endCpuTimeThrift = osBeanThrift.getProcessCpuTime();
            double totalCpuTimeThriftInSeconds = (endCpuTimeThrift - startCpuTimeThrift) / 1_000_000_000.0;


            System.out.println("  Ergebnisse:");
            System.out.println("    Größe (Bytes): " + thriftData.length);
            System.out.println("    Durchschnittliche Serialisierungszeit: " + (totalThriftSerializationTime / MEASUREMENT_RUNS) + " ns");
            System.out.println("    Durchschnittliche Deserialisierungszeit: " + (totalThriftDeserializationTime / MEASUREMENT_RUNS) + " ns");
            // Ausgabe der CPU-Zeit für Thrift
            System.out.println("    Gesamte CPU-Zeit für " + MEASUREMENT_RUNS + " Durchläufe: " + String.format("%.4f", totalCpuTimeThriftInSeconds) + " s");
        }
        // Ende des Thrift-Tests

                // Test für Protobuf
        System.out.println("\n[Protobuf]");
        ProtobufSerializer protoSerializer = new ProtobufSerializer();
        long totalProtoSerializationTime = 0;
        long totalProtoDeserializationTime = 0;
        byte[] protoData = protoSerializer.serialize(testPerson);

        // Korrektheitstest für Protobuf
        sqtest.Person deserializedProtoPersonCheck = protoSerializer.deserialize(protoData);
        if (deserializedProtoPersonCheck == null || !deserializedProtoPersonCheck.getName().equals(testPerson.getName())) {
            System.out.println("FEHLER: Deserialisierung für Protobuf fehlgeschlagen!");
        } else {
            // Warm-up für Protobuf
            System.out.println("  -> Führe Warm-up-Phase durch...");
            for (int i = 0; i < WARMUP_RUNS; i++) {
                protoSerializer.serialize(testPerson);
                protoSerializer.deserialize(protoData);
            }

            // Vorbereitung der CPU-Messung für Protobuf
            OperatingSystemMXBean osBeanProto = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double startCpuTimeProto = osBeanProto.getProcessCpuTime();

            // Messung für Protobuf
            System.out.println("  -> Führe Mess-Phase durch...");
            for (int i = 0; i < MEASUREMENT_RUNS; i++) {
                long start = System.nanoTime();
                byte[] data = protoSerializer.serialize(testPerson);
                long end = System.nanoTime();
                totalProtoSerializationTime += (end - start);

                start = System.nanoTime();
                protoSerializer.deserialize(data);
                end = System.nanoTime();
                totalProtoDeserializationTime += (end - start);
            }
            
            // CPU-Messung für Protobuf abschliessen
            double endCpuTimeProto = osBeanProto.getProcessCpuTime();
            double totalCpuTimeProtoInSeconds = (endCpuTimeProto - startCpuTimeProto) / 1_000_000_000.0;

            System.out.println("  Ergebnisse:");
            System.out.println("    Größe (Bytes): " + protoData.length);
            System.out.println("    Durchschnittliche Serialisierungszeit: " + (totalProtoSerializationTime / MEASUREMENT_RUNS) + " ns");
            System.out.println("    Durchschnittliche Deserialisierungszeit: " + (totalProtoDeserializationTime / MEASUREMENT_RUNS) + " ns");
            // Ausgabe der CPU-Zeit für Protobuf
            System.out.println("    Gesamte CPU-Zeit für " + MEASUREMENT_RUNS + " Durchläufe: " + String.format("%.4f", totalCpuTimeProtoInSeconds) + " s");
        }
        // Ende des Protobuf-Tests


        runTest("BSON", testPerson, new BsonSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);

        runTest("TOML", testPerson, new TomlSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);

        runTest("INI", testPerson, new IniSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);

        runTest("RDF (Turtle)", testPerson, new RdfTurtleSerializer(), WARMUP_RUNS, MEASUREMENT_RUNS);


        System.out.println("\n--- SQ-Test abgeschlossen ---");


        //============================================================
        // Stabilitätstest
        //============================================================

        System.out.println("\n\n--- Stabilitätstest gestartet ---");
        // Erstelle ein Person-Objekt mit problematischen Daten:
        // - Sonderzeichen und Emojis im Namen
        // - Null-Wert im Feld email
        Person problematicPerson = new Person("Max Müsster-Crönenberg 😊", 42, null);

        // --- Test für Schema-lose und Standard-Textformate ---
        System.out.println("\n--- Testgruppe: Schema-lose und Standard-Textformate ---");
        runStabilityTest("JSON", new JsonSerializer(), problematicPerson);
        runStabilityTest("XML", new XmlSerializer(), problematicPerson);
        runStabilityTest("CSV", new CsvSerializer(), problematicPerson);
        runStabilityTest("MessagePack", new MsgPackSerializer(), problematicPerson);
        runStabilityTest("CBOR", new CborSerializer(), problematicPerson);
        runStabilityTest("YAML", new YamlSerializer(), problematicPerson);
        runStabilityTest("BSON", new BsonSerializer(), problematicPerson);
        runStabilityTest("TOML", new TomlSerializer(), problematicPerson);
        runStabilityTest("INI", new IniSerializer(), problematicPerson);
        runStabilityTest("RDF (Turtle)", new RdfTurtleSerializer(), problematicPerson);


        // --- Test für Schema-basierte Formate (Avro, Protobuf, Thrift) ---
        // Diese benötigen eine spezielle Behandlung, da ihre Serializer nicht das generische
        // Interface implementieren und auf ihren eigenen, generierten Klassen basieren.
        // Das Testen hier zeigt, ob die Konvertierung zu und von ihren spezifischen
        // Klassen mit Null-Werten und Sonderzeichen umgehen kann.

        System.out.println("\n--- Testgruppe: Schema-basierte Formate ---");

        // Stabilitätstest für Avro
        System.out.println("\n[Stabilitätstest für: Avro]");
        try {
            AvroSerializer AvroSerializer = new AvroSerializer();
            avroData = AvroSerializer.serialize(problematicPerson);
            System.out.println("  -> Avro-Serialisierung erfolgreich.");
            sqtest.Person deserializedAvro = AvroSerializer.deserialize(avroData);
            if (problematicPerson.getName().equals(deserializedAvro.getName()) && deserializedAvro.getEmail() == null) {
                System.out.println("  -> Ergebnis: Robust. Datenintegrität gewahrt.");
            } else {
                System.out.println("  -> Ergebnis: Nicht Robust. Daten wurden verändert.");
            }
        } catch (Exception e) {
            System.out.println("  -> Ergebnis: Fehlerhaft. Prozess ist fehlgeschlagen mit Exception: " + e.getClass().getSimpleName());
        }

        // Stabilitätstest für Protobuf
        System.out.println("\n[Stabilitätstest für: Protobuf]");
        try {
            ProtobufSerializer ProtobufSerializer = new ProtobufSerializer();
            protoData = ProtobufSerializer.serialize(problematicPerson);
            System.out.println("  -> Protobuf-Serialisierung erfolgreich.");
            sqtest.Person deserializedProto = ProtobufSerializer.deserialize(protoData);
             // Protobuf kennt kein 'null' für Strings. Es wird zu einem leeren String "".
            if (problematicPerson.getName().equals(deserializedProto.getName()) && "".equals(deserializedProto.getEmail())) {
                System.out.println("  -> Ergebnis: Robust (aber 'null' wird zu leerem String). Datenintegrität interpretiert gewahrt.");
            } else {
                System.out.println("  -> Ergebnis: Nicht Robust. Daten wurden verändert.");
            }
        } catch (Exception e) {
            System.out.println("  -> Ergebnis: Fehlerhaft. Prozess ist fehlgeschlagen mit Exception: " + e.getClass().getSimpleName());
        }

        // Stabilitätstest für Thrift
        System.out.println("\n[Stabilitätstest für: Thrift]");
        try {
            ThriftSerializer ThriftSerializer = new ThriftSerializer();
            thriftData = ThriftSerializer.serialize(problematicPerson);
            System.out.println("  -> Thrift-Serialisierung erfolgreich.");
            sqtest.Person deserializedThrift = ThriftSerializer.deserialize(thriftData);
            if (problematicPerson.getName().equals(deserializedThrift.getName()) && deserializedThrift.getEmail() == null) {
                System.out.println("  -> Ergebnis: Robust. Datenintegrität gewahrt.");
            } else {
                System.out.println("  -> Ergebnis: Nicht Robust. Daten wurden verändert.");
            }
        } catch (Exception e) {
            System.out.println("  -> Ergebnis: Fehlerhaft. Prozess ist fehlgeschlagen mit Exception: " + e.getClass().getSimpleName());
            // e.printStackTrace(); // Zur Fehlersuche einkommentieren
        }

        System.out.println("\n--- Stabilitätstest abgeschlossen ---");
    }

    /**
     * Führt einen Performance-Test für einen gegebenen Serializer durch.
     * Der Test ist in eine Warm-up- und eine Messphase unterteilt.
     *
     * @param formatName        Der Name des Formats für die Ausgabe.
     * @param person            Das zu serialisierende Objekt.
     * @param serializer        Der zu testende Serializer.
     * @param warmupRuns        Anzahl der Durchläufe für die Warm-up-Phase.
     * @param measurementRuns   Anzahl der Durchläufe für die eigentliche Messung.
     * @throws Exception        Wenn bei der Serialisierung/Deserialisierung ein Fehler auftritt.
     */
    public static void runTest(String formatName, Person person, Serializer serializer, int warmupRuns, int measurementRuns) throws Exception {
        System.out.println("\n[" + formatName + "]");

        // Einmalige Messung der Grösse und Korrektheit ausserhalb der Schleife
        byte[] serializedData = serializer.serialize(person);
        long serializedSize = serializedData.length;
        Person deserializedPersonCheck = serializer.deserialize(serializedData, Person.class);
        if (deserializedPersonCheck == null || !deserializedPersonCheck.getName().equals(person.getName())) {
            System.out.println("FEHLER: Deserialisierung fehlgeschlagen!");
            return;
        }

        // ============================ WARM-UP PHASE ============================
        /*
         * BEGRÜNDUNG FÜR DIE WARM-UP-PHASE:
         * Die Java Virtual Machine (JVM) führt Code bei den ersten Malen oft langsamer aus.
         * Das liegt an einmaligen Operationen wie:
         * 1. Class Loading: Das Laden der benötigten Klassen in den Speicher.
         * 2. JIT-Kompilierung (Just-In-Time): Die JVM analysiert den häufig ausgeführten
         *    Code ("Hotspots") und kompiliert ihn zur Laufzeit in hochoptimierten Maschinencode.
         *    Die ersten Durchläufe finden oft noch im langsameren "interpretierten" Modus statt.
         *
         * Indem wir den zu testenden Code hier mehrfach ohne Messung ausführen, stellen wir
         * sicher, dass diese einmaligen Vorgänge abgeschlossen sind. Die anschliessenden
         * Messungen erfassen dann die stabile, optimierte "Steady-State"-Performance.
         * Für eine wissenschaftliche Arbeit ist dies essenziell, um verlässliche und
         * reproduzierbare Ergebnisse zu erhalten.
         */
        System.out.println("  -> Führe Warm-up-Phase durch...");
        for (int i = 0; i < warmupRuns; i++) {
            byte[] tempData = serializer.serialize(person);
            serializer.deserialize(tempData, Person.class);
        }
        // ======================== ENDE DER WARM-UP PHASE =======================


        // Variablen für die Mess-Phase zurücksetzen/initialisieren
        long totalSerializationTime = 0;
        long totalDeserializationTime = 0;
        
        // Vorbereitung der CPU-Messung
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double startCpuTime = osBean.getProcessCpuTime();

        // =========================== MESS-PHASE ==============================
        System.out.println("  -> Führe Mess-Phase durch...");
        for (int i = 0; i < measurementRuns; i++) {
            // Serialisierung messen
            long startSerialization = System.nanoTime();
            byte[] data = serializer.serialize(person);
            long endSerialization = System.nanoTime();
            totalSerializationTime += (endSerialization - startSerialization);

            // Deserialisierung messen
            long startDeserialization = System.nanoTime();
            serializer.deserialize(data, Person.class);
            long endDeserialization = System.nanoTime();
            totalDeserializationTime += (endDeserialization - startDeserialization);
        }
        // ======================== ENDE DER MESS-PHASE ========================

        // CPU-Messung abschliessen
        double endCpuTime = osBean.getProcessCpuTime();
        // Die Differenz ist die CPU-Zeit in Nanosekunden, die für die Mess-Phase benötigt wurde
        double totalCpuTimeInSeconds = (endCpuTime - startCpuTime) / 1_000_000_000.0;

        // Berechnung und Ausgabe der Durchschnittswerte
        long avgSerializationTime = totalSerializationTime / measurementRuns;
        long avgDeserializationTime = totalDeserializationTime / measurementRuns;

        System.out.println("  Ergebnisse:");
        System.out.println("    Größe (Bytes): " + serializedSize);
        System.out.println("    Durchschnittliche Serialisierungszeit: " + avgSerializationTime + " ns");
        System.out.println("    Durchschnittliche Deserialisierungszeit: " + avgDeserializationTime + " ns");
        // Ausgabe der CPU-Zeit
        System.out.println("    Gesamte CPU-Zeit für " + measurementRuns + " Durchläufe: " + String.format("%.4f", totalCpuTimeInSeconds) + " s");
    }

    public static void runStabilityTest(String formatName, Serializer serializer, Person problematicPerson) {
    System.out.println("\n[Stabilitätstest für: " + formatName + "]");
    try {
        // 1. Versuch, das problematische Objekt zu serialisieren
        byte[] serializedData = serializer.serialize(problematicPerson);
        System.out.println("  -> Serialisierung erfolgreich. Größe: " + serializedData.length + " Bytes.");

        // 2. Versuch, die Daten wieder zu deserialisieren
        Person deserializedPerson = serializer.deserialize(serializedData, Person.class);
        System.out.println("  -> Deserialisierung erfolgreich.");

        // 3. Überprüfen, ob die Daten erhalten geblieben sind
        boolean nameOk = problematicPerson.getName().equals(deserializedPerson.getName());
        boolean emailOk = problematicPerson.getEmail() == null ? deserializedPerson.getEmail() == null : problematicPerson.getEmail().equals(deserializedPerson.getEmail());
        
        if (nameOk && emailOk) {
            System.out.println("  -> Ergebnis: Robust. Datenintegrität gewahrt.");
        } else {
            System.out.println("  -> Ergebnis: Nicht Robust. Daten wurden bei der Übertragung verändert.");
        }

    } catch (Exception e) {
        // Wenn eine Exception auftritt, ist der Serializer nicht robust für diesen Fall.
        System.out.println("  -> Ergebnis: Fehlerhaft. Prozess ist fehlgeschlagen mit Exception: " + e.getClass().getSimpleName());
        
        // e.printStackTrace();
    }
}
}

//  mvn clean package

// mvn exec:java -Dexec.mainClass="sqtest.SQTestRunner"