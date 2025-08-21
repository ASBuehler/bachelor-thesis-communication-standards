package sqtest;

// Thrift-Imports werden nicht mehr benötigt.

public class ThriftSerializer implements Serializer {

    private static final String EXCLUSION_REASON = "Thrift was methodically excluded from the analysis due to unresolved and outdated Maven toolchain issues.";

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        // Wir werfen immer eine Exception, um die Exklusion zu dokumentieren.
        throw new UnsupportedOperationException(EXCLUSION_REASON);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        // Wir werfen immer eine Exception, um die Exklusion zu dokumentieren.
        throw new UnsupportedOperationException(EXCLUSION_REASON);
    }
}