package kketest.serializer;

import kketest.utility.MessErgebnis;

public interface Serializer {
    /**
     * Serialisiert ein Objekt und misst die dafür benötigte Zeit.
     * @param object Das zu serialisierende Objekt.
     * @param <T> Der Typ des Objekts.
     * @return Ein MessErgebnis, das das serialisierte byte[] und die Zeit in Nanosekunden enthält.
     * @throws Exception bei Serialisierungsfehlern.
     */
    <T> MessErgebnis<byte[]> serialize(T object) throws Exception;

    /**
     * Deserialisiert Daten in ein Objekt und misst die dafür benötigte Zeit.
     * @param data Das byte[] mit den serialisierten Daten.
     * @param clazz Die Klasse des Zielobjekts.
     * @param <T> Der Typ des Zielobjekts.
     * @return Ein MessErgebnis, das das deserialisierte Objekt und die Zeit in Nanosekunden enthält.
     * @throws Exception bei Deserialisierungsfehlern.
     */
    <T> MessErgebnis<T> deserialize(byte[] data, Class<T> clazz) throws Exception;
}