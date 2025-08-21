package sqtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Für LocalDate

public class YamlSerializer implements Serializer {

    private final ObjectMapper objectMapper;

    public YamlSerializer() {
        this.objectMapper = new ObjectMapper();
        
        
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        return objectMapper.writeValueAsBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        return objectMapper.readValue(data, clazz);
    }
}