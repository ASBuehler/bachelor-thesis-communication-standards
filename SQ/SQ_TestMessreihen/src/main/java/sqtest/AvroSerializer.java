package sqtest;

import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AvroSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (!(object instanceof DataSeries original)) {
            throw new IllegalArgumentException("AvroSerializer only supports DataSeries objects.");
        }

        // GEÄNDERT: Filtere problematische double-Werte heraus.
        List<Double> safeValues = null;
        if (original.getValues() != null) {
            safeValues = Arrays.stream(original.getValues())
                               .filter(d -> !Double.isNaN(d) && !Double.isInfinite(d))
                               .boxed()
                               .collect(Collectors.toList());
        }

        sqtest.avro.DataSeries avroDataSeries = sqtest.avro.DataSeries.newBuilder()
                .setSensorId(original.getSensorId())
                .setStartTimestamp(original.getStartTimestamp())
                .setUnit(original.getUnit()) // Funktioniert jetzt dank des neuen Schemas
                .setValues(safeValues)
                .build();

        DatumWriter<sqtest.avro.DataSeries> datumWriter = new SpecificDatumWriter<>(sqtest.avro.DataSeries.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        datumWriter.write(avroDataSeries, encoder);
        encoder.flush();
        return out.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (!clazz.equals(DataSeries.class)) {
            throw new IllegalArgumentException("AvroSerializer can only deserialize to DataSeries class.");
        }

        DatumReader<sqtest.avro.DataSeries> datumReader = new SpecificDatumReader<>(sqtest.avro.DataSeries.class);
        Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        sqtest.avro.DataSeries avroDataSeries = datumReader.read(null, decoder);

        return (T) new DataSeries(
                // Avro kann CharSequence zurückgeben, sicherheitshalber in String konvertieren
                avroDataSeries.getSensorId() != null ? avroDataSeries.getSensorId().toString() : null,
                avroDataSeries.getStartTimestamp(),
                avroDataSeries.getUnit() != null ? avroDataSeries.getUnit().toString() : null,
                avroDataSeries.getValues() != null ? avroDataSeries.getValues().stream().mapToDouble(d -> d).toArray() : null
        );
    }
}