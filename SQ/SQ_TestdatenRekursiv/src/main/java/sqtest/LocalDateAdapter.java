package sqtest;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
    public LocalDate unmarshal(String v) throws Exception {
        return (v == null) ? null : LocalDate.parse(v);
    }
    public String marshal(LocalDate v) throws Exception {
        return (v == null) ? null : v.toString();
    }
}