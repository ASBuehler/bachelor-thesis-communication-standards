package kketest.serializer;

public class SerializerFactory {

    /**
     * Gibt eine Instanz des passenden Serializers basierend auf dem Format-String zurück.
     * @param format Das gewünschte Format (z.B. "json" oder "xml").
     * @return Eine Implementierung des Serializer-Interfaces.
     * @throws IllegalArgumentException wenn das Format unbekannt ist.
     */
    public static Serializer getSerializer(String format) {
        if ("json".equalsIgnoreCase(format)) {
            return new JsonSerializer();
        } else if ("xml".equalsIgnoreCase(format)) {
            return new XmlSerializer();
        } else if ("messagepack".equalsIgnoreCase(format)) { 
            return new MessagePackSerializer();
        } else if ("cbor".equalsIgnoreCase(format)) { 
            return new CborSerializer();
        }
        // Hier können später weitere Formate wie "protobuf" hinzugefügt werden.
        
        throw new IllegalArgumentException("Unbekanntes Serialisierungsformat: " + format);
    }
}