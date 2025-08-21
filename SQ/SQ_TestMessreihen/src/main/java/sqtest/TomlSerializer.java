package sqtest;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TomlSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (!(object instanceof DataSeries)) {
             throw new IllegalArgumentException("TOML serialization in this test is only supported for DataSeries objects.");
        }
        
        // Konvertiere das DataSeries-Objekt in eine 'sichere' Map
        Map<String, Object> tomlMap = convertDataSeriesToMap((DataSeries) object);

        TomlWriter tomlWriter = new TomlWriter.Builder().build();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos)) {
            tomlWriter.write(tomlMap, writer);
            writer.flush();
            return baos.toByteArray();
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (!clazz.equals(DataSeries.class)) {
            throw new IllegalArgumentException("TOML deserialization in this test is only supported for DataSeries objects.");
        }
        
        Toml toml = new Toml().read(new String(data));
        return (T) convertTomlToDataSeries(toml);
    }
    
    // --- Helfermethoden für die manuelle Konvertierung ---

    private Map<String, Object> convertDataSeriesToMap(DataSeries dataSeries) {
        Map<String, Object> map = new LinkedHashMap<>();
        
        // Füge Felder nur hinzu, wenn sie nicht null sind
        if (dataSeries.getSensorId() != null) map.put("sensorId", dataSeries.getSensorId());
        map.put("startTimestamp", dataSeries.getStartTimestamp());
        if (dataSeries.getUnit() != null) map.put("unit", dataSeries.getUnit());
        
        // Konvertiere double-Array zu einer Liste von Doubles (TOML kann das)
        if (dataSeries.getValues() != null) {
            // WICHTIG: Filtere NaN und Infinity heraus, da TOML sie nicht unterstützt
            List<Double> valueList = Arrays.stream(dataSeries.getValues())
                                             .filter(d -> !Double.isNaN(d) && !Double.isInfinite(d))
                                             .boxed()
                                             .collect(Collectors.toList());
            map.put("values", valueList);
        }
        return map;
    }
    
    private DataSeries convertTomlToDataSeries(Toml toml) {
        String sensorId = toml.getString("sensorId");
        long timestamp = toml.getLong("startTimestamp", 0L);
        String unit = toml.getString("unit"); // Gibt null zurück, wenn nicht vorhanden
        
        List<Double> valueList = toml.getList("values");
        double[] values = (valueList != null) 
            ? valueList.stream().mapToDouble(d -> d).toArray() 
            : new double[0];
            
        return new DataSeries(sensorId, timestamp, unit, values);
    }
}