package sqtest;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CsvSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (!(object instanceof DataSeries dataSeries)) {
            throw new IllegalArgumentException("CSV serialization in this test is only supported for DataSeries objects.");
        }

        StringBuilder sb = new StringBuilder();

        // Zeile 1: Header-Daten (sensorId,timestamp,unit)
        sb.append(escape(dataSeries.getSensorId())).append(",");
        sb.append(dataSeries.getStartTimestamp()).append(",");
        sb.append(escape(dataSeries.getUnit()));
        sb.append("\n");

        // Zeile 2: Alle double-Werte, mit Komma getrennt
        if (dataSeries.getValues() != null) {
            String valuesString = Arrays.stream(dataSeries.getValues())
                                        .mapToObj(String::valueOf)
                                        .collect(Collectors.joining(","));
            sb.append(valuesString);
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (!clazz.equals(DataSeries.class)) {
            throw new IllegalArgumentException("CSV deserialization in this test is only supported for DataSeries objects.");
        }

        String csvContent = new String(data, StandardCharsets.UTF_8);
        String[] lines = csvContent.split("\n", 2); // Teile in maximal 2 Zeilen

        if (lines.length < 1) {
            throw new IllegalStateException("CSV data is empty.");
        }

        // Parse Zeile 1: Header
        String[] headerParts = lines[0].split(",", -1);
        String sensorId = unescape(headerParts[0]);
        long timestamp = Long.parseLong(headerParts[1]);
        String unit = unescape(headerParts[2]);

        // Parse Zeile 2: Werte
        double[] values = new double[0];
        if (lines.length > 1 && !lines[1].isEmpty()) {
            values = Arrays.stream(lines[1].split(","))
                           .mapToDouble(Double::parseDouble)
                           .toArray();
        }

        return (T) new DataSeries(sensorId, timestamp, unit, values);
    }

    // Einfache Escape/Unescape-Funktionen für CSV
    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
    
    private String unescape(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }
}