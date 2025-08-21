package sqtest;

import org.ini4j.Ini;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

public class IniSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (!(object instanceof DataSeries dataSeries)) {
            throw new IllegalArgumentException("INI serialization in this test is only supported for DataSeries objects.");
        }

        Ini ini = new Ini();
        
        // Header-Daten in die [header] Sektion schreiben
        ini.put("header", "sensorId", dataSeries.getSensorId());
        ini.put("header", "startTimestamp", dataSeries.getStartTimestamp());
        ini.put("header", "unit", dataSeries.getUnit());

        // Daten als einen langen String in die [data] Sektion schreiben
        if (dataSeries.getValues() != null) {
            String valuesString = Arrays.stream(dataSeries.getValues())
                                        .mapToObj(String::valueOf)
                                        .collect(Collectors.joining(","));
            ini.put("data", "values", valuesString);
        } else {
            ini.put("data", "values", "");
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ini.store(out);
        return out.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (!clazz.equals(DataSeries.class)) {
            throw new IllegalArgumentException("INI deserialization in this test is only supported for DataSeries objects.");
        }

        Ini ini = new Ini(new ByteArrayInputStream(data));
        
        // Lese Header-Daten
        String sensorId = ini.get("header", "sensorId");
        long timestamp = ini.get("header", "startTimestamp", long.class);
        String unit = ini.get("header", "unit");
        
        // Lese und parse die Daten
        String valuesString = ini.get("data", "values");
        double[] values = new double[0];
        if (valuesString != null && !valuesString.isEmpty()) {
            values = Arrays.stream(valuesString.split(","))
                           .mapToDouble(Double::parseDouble)
                           .toArray();
        }

        return (T) new DataSeries(sensorId, timestamp, unit, values);
    }
}