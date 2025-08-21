package sqtest;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class TomlSerializer implements Serializer {

    private final TomlWriter tomlWriter;

    public TomlSerializer() {

        this.tomlWriter = new TomlWriter.Builder().build();
    }

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        tomlWriter.write(object, out);
        return out.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        String tomlString = new String(data, StandardCharsets.UTF_8);
        return new Toml().read(tomlString).to(clazz);
    }
}