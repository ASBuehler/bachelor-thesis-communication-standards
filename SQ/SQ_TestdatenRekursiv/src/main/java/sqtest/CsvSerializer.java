package sqtest;

import java.nio.charset.StandardCharsets;

public class CsvSerializer implements Serializer { // Interface angepasst

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (object instanceof OrganizationalChartNode) {
            throw new UnsupportedOperationException("CSV is a flat, tabular format and does not support nested/recursive structures like OrganizationalChartNode.");
        }
        // Hier könnte die alte Logik für Person stehen, wenn du sie bräuchtest.
        // Für diesen Test reicht die Exception.
        throw new IllegalArgumentException("CSV format not supported for this object type: " + object.getClass().getName());
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (clazz.equals(OrganizationalChartNode.class)) {
            throw new UnsupportedOperationException("Cannot deserialize a flat CSV into a nested/recursive structure like OrganizationalChartNode.");
        }
        throw new IllegalArgumentException("CSV format not supported for this object type: " + clazz.getName());
    }
}