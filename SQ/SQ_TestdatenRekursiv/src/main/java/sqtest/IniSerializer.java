package sqtest;

// Imports werden nicht mehr benötigt, da wir eine Exception werfen.
// import org.ini4j.Ini;
// import java.io.ByteArrayInputStream;
// import java.io.ByteArrayOutputStream;

public class IniSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (object instanceof OrganizationalChartNode) {
            throw new UnsupportedOperationException("INI is a flat key-value format and does not support nested/recursive structures like OrganizationalChartNode.");
        }
        // Für alle anderen unbekannten Typen ebenfalls eine Exception werfen.
        throw new IllegalArgumentException("INI format not supported for this object type: " + object.getClass().getName());
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (clazz.equals(OrganizationalChartNode.class)) {
            throw new UnsupportedOperationException("Cannot deserialize a flat INI into a nested/recursive structure like OrganizationalChartNode.");
        }
        throw new IllegalArgumentException("INI format not supported for this object type: " + clazz.getName());
    }
}