// src/main/java/sqtest/LocalDateAdapter.java
package sqtest;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public LocalDate unmarshal(String v) throws Exception {
        // Konvertiert den String aus dem XML in ein LocalDate-Objekt
        if (v == null) {
            return null;
        }
        return LocalDate.parse(v, formatter);
    }

    @Override
    public String marshal(LocalDate v) throws Exception {
        // Konvertiert das LocalDate-Objekt in einen String für das XML
        if (v == null) {
            return null;
        }
        return v.format(formatter);
    }
}