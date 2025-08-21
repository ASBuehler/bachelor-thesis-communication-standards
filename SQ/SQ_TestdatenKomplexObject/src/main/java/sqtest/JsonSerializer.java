package sqtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonSerializer implements Serializer {

    private final ObjectMapper objectMapper;

    public JsonSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (InvalidDefinitionException e) {
            
            String detailedMessage = String.format(
                "Jackson Fehler bei der Serialisierung von Typ: %s. Ursprüngliche Nachricht: %s",
                e.getType().getRawClass().getName(), 
                e.getOriginalMessage()
            );
            throw new Exception(detailedMessage, e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (InvalidDefinitionException e) {
            String detailedMessage = String.format(
                "Jackson Fehler bei der Deserialisierung von Typ: %s. Ursprüngliche Nachricht: %s",
                e.getType().getRawClass().getName(),
                e.getOriginalMessage()
            );
            throw new Exception(detailedMessage, e);
        }
    }
}