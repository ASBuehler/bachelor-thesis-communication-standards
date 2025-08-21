package kketest.protocol;

import kketest.utility.MessErgebnis;

public interface Sender {
    /**
     * Sendet die gegebenen Daten und misst die Round-Trip Time.
     * @param data Die zu sendenden Daten als byte-Array.
     * @return Ein Messergebnis, das die RTT in Nanosekunden enthält.
     * @throws Exception bei Netzwerkfehlern.
     */
    MessErgebnis<Void> send(byte[] data) throws Exception;
}