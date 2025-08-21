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
            // Der Context muss jetzt nur noch den XmlDataSeries-Wrapper kennen.
            CONTEXT = JAXBContext.newInstance(XmlDataSeries.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to initialize JAXBContext for XmlDataSeries", e);
        }
    }

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (!(object instanceof DataSeries)) {
            throw new IllegalArgumentException("XmlSerializer in this project only supports DataSeries.");
        }
        
        // Konvertiere das DataSeries-Objekt in seinen XML-Wrapper.
        XmlDataSeries xmlWrapper = new XmlDataSeries((DataSeries) object);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Marshaller marshaller = CONTEXT.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        
        marshaller.marshal(xmlWrapper, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (!clazz.equals(DataSeries.class)) {
            throw new IllegalArgumentException("XmlSerializer can only deserialize to DataSeries class.");
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        Unmarshaller unmarshaller = CONTEXT.createUnmarshaller();
        XmlDataSeries xmlWrapper = (XmlDataSeries) unmarshaller.unmarshal(inputStream);

        // Konvertiere den Wrapper zurück in das ursprüngliche DataSeries-Objekt.
        return (T) xmlWrapper.toDataSeries();
    }
}