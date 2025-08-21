package sqtest;

import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AvroSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (object instanceof OrganizationalChartNode node) {
            sqtest.avro.OrganizationalChartNode avroNode = convertToAvroNode(node);
            
            DatumWriter<sqtest.avro.OrganizationalChartNode> datumWriter = new SpecificDatumWriter<>(sqtest.avro.OrganizationalChartNode.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            datumWriter.write(avroNode, encoder);
            encoder.flush();
            return out.toByteArray();
        }
        throw new IllegalArgumentException("AvroSerializer only supports OrganizationalChartNode, not " + object.getClass().getName());
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (clazz.equals(OrganizationalChartNode.class)) {
            DatumReader<sqtest.avro.OrganizationalChartNode> datumReader = new SpecificDatumReader<>(sqtest.avro.OrganizationalChartNode.class);
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            sqtest.avro.OrganizationalChartNode avroNode = datumReader.read(null, decoder);
            
            return (T) convertFromAvroNode(avroNode);
        }
        throw new IllegalArgumentException("AvroSerializer only supports OrganizationalChartNode, not " + clazz.getName());
    }

    /**
     * Konvertiert rekursiv einen POJO-Knoten in einen von Avro generierten Knoten.
     */
    private sqtest.avro.OrganizationalChartNode convertToAvroNode(OrganizationalChartNode original) {
        if (original == null) return null;

        List<sqtest.avro.OrganizationalChartNode> avroSubordinates = new ArrayList<>();
        if (original.getSubordinates() != null) {
            for (OrganizationalChartNode sub : original.getSubordinates()) {
                avroSubordinates.add(convertToAvroNode(sub)); 
            }
        }
        
        Map<CharSequence, CharSequence> avroMetadata = null;
        if (original.getMetadata() != null) {
            // Wir erstellen eine neue Map mit dem von Avro erwarteten Typ.
            avroMetadata = new HashMap<>();
            for (Map.Entry<String, String> entry : original.getMetadata().entrySet()) {
                avroMetadata.put(entry.getKey(), entry.getValue());
            }
        }
        
        return sqtest.avro.OrganizationalChartNode.newBuilder()
                .setId(original.getId())
                .setName(original.getName())
                .setRole(original.getRole())
                .setJoinedDate(original.getJoinedDate())
                .setMetadata(avroMetadata) // Verwende die neu erstellte, typkorrekte Map
                .setSubordinates(avroSubordinates)
                .build();
    }

    /**
     * Konvertiert rekursiv einen von Avro generierten Knoten zurück in einen POJO-Knoten.
     */
    private OrganizationalChartNode convertFromAvroNode(sqtest.avro.OrganizationalChartNode avroNode) {
        if (avroNode == null) return null;

        List<OrganizationalChartNode> subordinates = new ArrayList<>();
        if (avroNode.getSubordinates() != null) {
            for (sqtest.avro.OrganizationalChartNode avroSub : avroNode.getSubordinates()) {
                subordinates.add(convertFromAvroNode(avroSub));
            }
        }
        
        OrganizationalChartNode original = new OrganizationalChartNode();
        original.setId(avroNode.getId());
        original.setName(avroNode.getName() != null ? avroNode.getName().toString() : null);
        original.setRole(avroNode.getRole() != null ? avroNode.getRole().toString() : null);
        original.setJoinedDate(avroNode.getJoinedDate());

        if (avroNode.getMetadata() != null) {
             original.setMetadata(avroNode.getMetadata().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString())));
        }
        original.setSubordinates(subordinates);
        
        return original;
    }
}