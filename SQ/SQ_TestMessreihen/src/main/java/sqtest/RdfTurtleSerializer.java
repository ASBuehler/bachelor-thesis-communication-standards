package sqtest;

public class RdfTurtleSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T object) {
        throw new UnsupportedOperationException("RDF format is not suited for serializing large numerical arrays in this context.");
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        throw new UnsupportedOperationException("RDF format is not suited for deserializing large numerical arrays in this context.");
    }
}