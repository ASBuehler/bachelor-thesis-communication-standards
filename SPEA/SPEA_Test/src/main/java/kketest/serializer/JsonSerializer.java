package kketest.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import kketest.utility.MessErgebnis;

public class JsonSerializer implements Serializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> MessErgebnis<byte[]> serialize(T object) throws Exception {
        long startTime = System.nanoTime();
        byte[] data = objectMapper.writeValueAsBytes(object);
        long endTime = System.nanoTime();
        
        // Geben Sie die Payload-Größe direkt hier aus, da sie hier bekannt ist
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