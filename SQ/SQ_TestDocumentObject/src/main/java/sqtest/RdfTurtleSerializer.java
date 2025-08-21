package sqtest;


public class RdfTurtleSerializer implements Serializer { 

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (object instanceof OrganizationalChartNode) {
            throw new UnsupportedOperationException("RDF Turtle requires a specific graph model. Direct POJO recursion is not supported without a custom mapping layer.");
        }
        throw new IllegalArgumentException("RDF format not supported for this object type: " + object.getClass().getName());
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (clazz.equals(OrganizationalChartNode.class)) {
            throw new UnsupportedOperationException("Cannot deserialize an RDF graph into a recursive POJO structure without a custom mapping layer.");
        }
        throw new IllegalArgumentException("RDF format not supported for this object type: " + clazz.getName());
    }
}