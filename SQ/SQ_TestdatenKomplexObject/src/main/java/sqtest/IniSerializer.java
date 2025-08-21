package sqtest;

import org.ini4j.Ini;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class IniSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (!(object instanceof Person person)) {
            throw new IllegalArgumentException("Only Person object is supported for INI serialization.");
        }

        Ini ini = new Ini();
        ini.put("person", "name", person.getName());
        ini.put("person", "age", person.getAge());
        ini.put("person", "email", person.getEmail());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ini.store(out);
        return out.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (!clazz.equals(Person.class)) {
            throw new IllegalArgumentException("Only Person object is supported for INI deserialization.");
        }

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        Ini ini = new Ini(in);

        String name = ini.get("person", "name");
        int age = ini.get("person", "age", int.class);
        String email = ini.get("person", "email");

        return (T) new Person(name, age, email);
    }
}