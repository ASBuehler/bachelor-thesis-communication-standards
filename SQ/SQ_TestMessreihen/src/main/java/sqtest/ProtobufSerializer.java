package sqtest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProtobufSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (!(object instanceof DataSeries original)) {
            throw new IllegalArgumentException("ProtobufSerializer only supports DataSeries objects.");
        }

        sqtest.proto.DataSeriesProto.DataSeries.Builder builder = sqtest.proto.DataSeriesProto.DataSeries.newBuilder();
        
        // Prüfe auf null, bevor die Setter aufgerufen werden
        if (original.getSensorId() != null) builder.setSensorId(original.getSensorId());
        builder.setStartTimestamp(original.getStartTimestamp());
        if (original.getUnit() != null) builder.setUnit(original.getUnit());

        // Filtere problematische double-Werte heraus
        if (original.getValues() != null) {
            List<Double> safeValues = Arrays.stream(original.getValues())
                                            .filter(d -> !Double.isNaN(d) && !Double.isInfinite(d))
                                            .boxed()
                                            .collect(Collectors.toList());
            builder.addAllValues(safeValues);
        }
        
        return builder.build().toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (!clazz.equals(DataSeries.class)) {
            throw new IllegalArgumentException("ProtobufSerializer can only deserialize to DataSeries class.");
        }
        
        sqtest.proto.DataSeriesProto.DataSeries protoDataSeries = sqtest.proto.DataSeriesProto.DataSeries.parseFrom(data);

        // Beim Deserialisieren ist ein nicht gesetzter String ein leerer String.
        // Wir müssen entscheiden, ob wir das als `null` interpretieren wollen.
        // Für den Stabilitätstest ist es besser, `null` zu erwarten.
        String unit = protoDataSeries.getUnit().isEmpty() ? null : protoDataSeries.getUnit();

        return (T) new DataSeries(
                protoDataSeries.getSensorId(),
                protoDataSeries.getStartTimestamp(),
                unit,
                protoDataSeries.getValuesList().stream().mapToDouble(d -> d).toArray()
        );
    }
}