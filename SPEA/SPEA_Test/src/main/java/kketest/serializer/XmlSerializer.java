package kketest.serializer;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kketest.model.DataObject;
import kketest.utility.MessErgebnis;

public class XmlSerializer implements Serializer {

    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public <T> MessErgebnis<byte[]> serialize(T object) throws Exception {
        long startTime = System.nanoTime();
        byte[] data = xmlMapper.writeValueAsBytes(object);
        long endTime = System.nanoTime();
        
        // Geben Sie die Payload-Größe direkt hier aus
        System.out.println("PAYLOAD_SIZE_bytes=" + data.length);

        return new MessErgebnis<>(data, endTime - startTime);
    }

    @Override
    public <T> MessErgebnis<T> deserialize(byte[] data, Class<T> clazz) throws Exception {
        long startTime = System.nanoTime();
        T obj = xmlMapper.readValue(data, clazz);
        long endTime = System.nanoTime();
        
        return new MessErgebnis<>(obj, endTime - startTime);
    }
}