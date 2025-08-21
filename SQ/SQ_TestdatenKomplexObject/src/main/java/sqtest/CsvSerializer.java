package sqtest;

import java.nio.charset.StandardCharsets;

public class CsvSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (!(object instanceof Person person)) {
            throw new IllegalArgumentException("Only Person supported");
        }
        String csv = String.format("%s,%d,%s", person.getName(), person.getAge(), person.getEmail());
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (!clazz.equals(Person.class)) {
            throw new IllegalArgumentException("Only Person supported");
        }
        String csv = new String(data, StandardCharsets.UTF_8);
        String[] parts = csv.split(",", -1);
        String name = parts[0];
        int age = Integer.parseInt(parts[1]);
        String email = parts[2];
        return (T) new Person(name, age, email);
    }
}
