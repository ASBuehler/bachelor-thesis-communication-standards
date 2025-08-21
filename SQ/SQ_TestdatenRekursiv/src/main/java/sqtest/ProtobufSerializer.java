package sqtest;

import com.google.protobuf.InvalidProtocolBufferException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class ProtobufSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        
        if (object instanceof OrganizationalChartNode node) {
            sqtest.proto.OrganizationalChartNodeProto.OrganizationalChartNode protoNode = convertToProtoNode(node);
            return protoNode.toByteArray();
        }
        throw new IllegalArgumentException("ProtobufSerializer only supports OrganizationalChartNode, not " + object.getClass().getName());
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        
        if (clazz.equals(OrganizationalChartNode.class)) {
            sqtest.proto.OrganizationalChartNodeProto.OrganizationalChartNode protoNode = sqtest.proto.OrganizationalChartNodeProto.OrganizationalChartNode.parseFrom(data);
            return (T) convertFromProtoNode(protoNode);
        }
        throw new IllegalArgumentException("ProtobufSerializer only supports OrganizationalChartNode, not " + clazz.getName());
    }


    /**
     * Konvertiert rekursiv einen POJO-Knoten in einen von Protobuf generierten Knoten.s
     */
    private sqtest.proto.OrganizationalChartNodeProto.OrganizationalChartNode convertToProtoNode(OrganizationalChartNode original) {
        if (original == null) return null;

        sqtest.proto.OrganizationalChartNodeProto.OrganizationalChartNode.Builder builder = 
            sqtest.proto.OrganizationalChartNodeProto.OrganizationalChartNode.newBuilder();
        
        builder.setId(original.getId());
        if (original.getName() != null) builder.setName(original.getName());
        if (original.getRole() != null) builder.setRole(original.getRole());
        // Unser Schema verwendet String für das Datum
        if (original.getJoinedDate() != null) builder.setJoinedDate(original.getJoinedDate().toString());
        if (original.getMetadata() != null) builder.putAllMetadata(original.getMetadata());
        
        // Rekursive Konvertierung der Untergebenen
        if (original.getSubordinates() != null) {
            for (OrganizationalChartNode sub : original.getSubordinates()) {
                builder.addSubordinates(convertToProtoNode(sub)); // Rekursiver Aufruf
            }
        }
        
        return builder.build();
    }

    /**
     * Konvertiert rekursiv einen von Protobuf generierten Knoten zurück in einen POJO-Knoten.
     */
    private OrganizationalChartNode convertFromProtoNode(sqtest.proto.OrganizationalChartNodeProto.OrganizationalChartNode protoNode) {
        if (protoNode == null) return null;

        OrganizationalChartNode original = new OrganizationalChartNode();
        original.setId(protoNode.getId());
        original.setName(protoNode.getName());
        original.setRole(protoNode.getRole());
        // Konvertiere String zurück zu LocalDate
        if (protoNode.getJoinedDate() != null && !protoNode.getJoinedDate().isEmpty()) {
            original.setJoinedDate(LocalDate.parse(protoNode.getJoinedDate()));
        }
        original.setMetadata(protoNode.getMetadataMap());

        // Rekursive Konvertierung der Untergebenen
        if (protoNode.getSubordinatesList() != null) {
            List<OrganizationalChartNode> subordinates = new ArrayList<>();
            for (sqtest.proto.OrganizationalChartNodeProto.OrganizationalChartNode protoSub : protoNode.getSubordinatesList()) {
                subordinates.add(convertFromProtoNode(protoSub)); // Rekursiver Aufruf
            }
            original.setSubordinates(subordinates);
        }
        
        return original;
    }
}