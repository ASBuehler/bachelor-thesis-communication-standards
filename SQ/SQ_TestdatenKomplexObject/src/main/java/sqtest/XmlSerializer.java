package sqtest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class XmlSerializer implements Serializer {

    private static final JAXBContext CONTEXT;

    static {
        try {
            // Der Context muss jetzt BEIDE XML-Wrapper-Klassen kennen!
            CONTEXT = JAXBContext.newInstance(XmlPerson.class, XmlPersonAdvanced.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Fehler bei der Initialisierung des JAXBContext", e);
        }
    }

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        Object xmlWrapper;

        // Hier wird unterschieden, welcher Wrapper verwendet wird.
        if (object instanceof PersonAdvanced) {
            xmlWrapper = new XmlPersonAdvanced((PersonAdvanced) object);
        } else if (object instanceof Person) {
            xmlWrapper = new XmlPerson((Person) object);
        } else {
            throw new IllegalArgumentException("Unbekannter Objekttyp für XML-Serialisierung: " + object.getClass().getName());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Marshaller marshaller = CONTEXT.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        
        marshaller.marshal(xmlWrapper, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        Unmarshaller unmarshaller = CONTEXT.createUnmarshaller();
        Object xmlWrapper = unmarshaller.unmarshal(inputStream);

        // Hier wird unterschieden, welcher Wrapper zurückkonvertiert wird.
        if (xmlWrapper instanceof XmlPersonAdvanced) {
            return (T) ((XmlPersonAdvanced) xmlWrapper).toPersonAdvanced();
        } else if (xmlWrapper instanceof XmlPerson) {
            return (T) ((XmlPerson) xmlWrapper).toPerson();
        } else {
            throw new IllegalArgumentException("Unbekannter Objekttyp nach XML-Deserialisierung: " + xmlWrapper.getClass().getName());
        }
    }
}