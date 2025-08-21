package kketest.protocol;

public interface Receiver {
    /**
     * Startet den Empfänger und wartet auf eine einzelne Anfrage.
     * @throws Exception bei Netzwerkfehlern.
     */
    void listen() throws Exception;

    /**
     * Stoppt den laufenden Server.
     */
    void stop();
}