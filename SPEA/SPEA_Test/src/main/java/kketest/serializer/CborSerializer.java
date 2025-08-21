package kketest.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import kketest.utility.MessErgebnis;

public class CborSerializer implements Serializer {

    // Der Schlüssel ist, dem ObjectMapper die CBORFactory zu übergeben.
    private final ObjectMapper objectMapper = new ObjectMapper(new CBORFactory());

    @Override
    public <T> MessErgebnis<byte[]> serialize(T object) throws Exception {
        long startTime = System.nanoTime();
        byte[] data = objectMapper.writeValueAsBytes(object);
        long endTime = System.nanoTime();
        
        System.out.println("PAYLOAD_SIZE_bytes=" + data.length);

        return new MessErgebnis<>(data, endTime - startTime);
    }

    @Override
    public <T> MessErgebnis<T> deserialize(byte[] data, Class<T> clazz) throws Exception {
        long startTime = System.nanoTime();
        T obj = objectMapper.readValue(data, clazz);
        long endTime = System.nanoTime();
        
        return new MessErgebnis<>(obj, endTime - startTime);
    }
}