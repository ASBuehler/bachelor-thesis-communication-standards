package sqtest;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class StatusAdapter extends XmlAdapter<String, Status> {
    public Status unmarshal(String v) throws Exception {
        return (v == null) ? null : Status.valueOf(v);
    }
    public String marshal(Status v) throws Exception {
        return (v == null) ? null : v.name();
    }
}