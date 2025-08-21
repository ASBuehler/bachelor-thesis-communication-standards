package sqtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Für LocalDate

public class BsonSerializer implements Serializer {

    private final ObjectMapper objectMapper;

    public BsonSerializer() {
        this.objectMapper = new ObjectMapper();
        
        // DIESE ZEILE HINZUFÜGEN:
        // Registriert das Modul, damit Jackson mit Java 8+ Zeit-Typen umgehen kann.
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