package kketest.utility;

public class MessErgebnis<T> {
    public final T objekt;
    public final long zeitInNs;

    public MessErgebnis(T objekt, long zeitInNs) {
        this.objekt = objekt;
        this.zeitInNs = zeitInNs;
    }
}