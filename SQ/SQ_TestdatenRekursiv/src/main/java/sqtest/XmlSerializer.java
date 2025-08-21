package sqtest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (object instanceof OrganizationalChartNode node) {
            // Wir initialisieren die 'visited' Map hier für jeden neuen Serialisierungsaufruf.
            XmlOrganizationalChartNode xmlNode = toXmlDto(node, new HashMap<>());
            
            JAXBContext context = JAXBContext.newInstance(XmlOrganizationalChartNode.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            marshaller.marshal(xmlNode, out);
            return out.toByteArray();
        }
        throw new IllegalArgumentException("XML serialization for type " + object.getClass().getName() + " is not implemented.");
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (clazz.equals(OrganizationalChartNode.class)) {
            JAXBContext context = JAXBContext.newInstance(XmlOrganizationalChartNode.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            XmlOrganizationalChartNode xmlNode = (XmlOrganizationalChartNode) unmarshaller.unmarshal(new ByteArrayInputStream(data));
            
            // Auch hier initialisieren wir die 'visited' Map.
            return (T) fromXmlDto(xmlNode, new HashMap<>());
        }
        throw new IllegalArgumentException("XML deserialization for type " + clazz.getName() + " is not implemented.");
    }

    
    private XmlOrganizationalChartNode toXmlDto(OrganizationalChartNode original, Map<OrganizationalChartNode, XmlOrganizationalChartNode> visited) {
        if (original == null) return null;
        
        // ZYKLUSERKENNUNG: Wenn wir diesen Knoten schon besucht haben, brechen wir die Rekursion ab.
        if (visited.containsKey(original)) {
            return visited.get(original); // Gib die bereits erstellte DTO-Instanz zurück.
        }

        XmlOrganizationalChartNode dto = new XmlOrganizationalChartNode();
        visited.put(original, dto); // Markiere diesen Knoten als besucht, BEVOR wir in die Tiefe gehen.

        dto.setId(original.getId());
        dto.setName(original.getName());
        dto.setRole(original.getRole());
        dto.setJoinedDate(original.getJoinedDate());

        if (original.getSubordinates() != null) {
            List<XmlOrganizationalChartNode> dtoSubordinates = new ArrayList<>();
            for (OrganizationalChartNode sub : original.getSubordinates()) {
                dtoSubordinates.add(toXmlDto(sub, visited)); // Gib die 'visited' Map weiter.
            }
            dto.setSubordinates(dtoSubordinates);
        }
        return dto;
    }

    private OrganizationalChartNode fromXmlDto(XmlOrganizationalChartNode dto, Map<XmlOrganizationalChartNode, OrganizationalChartNode> visited) {
        if (dto == null) return null;

        if (visited.containsKey(dto)) {
            return visited.get(dto);
        }

        OrganizationalChartNode original = new OrganizationalChartNode();
        visited.put(dto, original);

        original.setId(dto.getId());
        original.setName(dto.getName());
        original.setRole(dto.getRole());
        original.setJoinedDate(dto.getJoinedDate());

        if (dto.getSubordinates() != null) {
            List<OrganizationalChartNode> originalSubordinates = new ArrayList<>();
            for (XmlOrganizationalChartNode subDto : dto.getSubordinates()) {
                originalSubordinates.add(fromXmlDto(subDto, visited));
            }
            original.setSubordinates(originalSubordinates);
        }
        return original;
    }
}