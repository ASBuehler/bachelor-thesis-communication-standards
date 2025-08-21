package sqtest;

public interface Serializer {
    <T> byte[] serialize(T object) throws Exception;
    <T> T deserialize(byte[] data, Class<T> clazz) throws Exception;
}
