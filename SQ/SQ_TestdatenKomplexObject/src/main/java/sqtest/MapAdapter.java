package sqtest;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class MapAdapter extends XmlAdapter<MapAdapter.AdaptedMap, Map<String, String>> {

    // Innere Klasse zur Darstellung eines einzelnen Eintrags (key-value)
    public static class AdaptedEntry {
        public String key;
        public String value;
    }

    // Innere Klasse zur Darstellung der gesamten Map als Liste von Einträgen
    public static class AdaptedMap {
        public List<AdaptedEntry> entry = new ArrayList<>();
    }

    // Wandelt die XML-Form (AdaptedMap) zurück in eine Java-Map
    @Override
    public Map<String, String> unmarshal(AdaptedMap adaptedMap) throws Exception {
        if (adaptedMap == null) return null;
        Map<String, String> map = new HashMap<>();
        for (AdaptedEntry entry : adaptedMap.entry) {
            map.put(entry.key, entry.value);
        }
        return map;
    }

    // Wandelt eine Java-Map in die XML-Form (AdaptedMap) um
    @Override
    public AdaptedMap marshal(Map<String, String> map) throws Exception {
        if (map == null) return null;
        AdaptedMap adaptedMap = new AdaptedMap();
        for (Map.Entry<String, String> mapEntry : map.entrySet()) {
            AdaptedEntry adaptedEntry = new AdaptedEntry();
            adaptedEntry.key = mapEntry.getKey();
            adaptedEntry.value = mapEntry.getValue();
            adaptedMap.entry.add(adaptedEntry);
        }
        return adaptedMap;
    }
}