package sqtest;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.VCARD;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class RdfTurtleSerializer implements Serializer {

    private static final String PERSON_URI = "http://example.org/person";

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (!(object instanceof Person person)) {
            throw new IllegalArgumentException("Only Person object is supported for RDF serialization.");
        }

        Model model = ModelFactory.createDefaultModel();
        Resource personResource = model.createResource(PERSON_URI)
                .addProperty(VCARD.FN, person.getName())
                .addProperty(model.createProperty("http://example.org/property/age"), String.valueOf(person.getAge()));

        if (person.getEmail() != null) {
            personResource.addProperty(VCARD.EMAIL, person.getEmail());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFDataMgr.write(out, model, Lang.TURTLE);
        return out.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (!clazz.equals(Person.class)) {
            throw new IllegalArgumentException("Only Person object is supported for RDF deserialization.");
        }

        Model model = ModelFactory.createDefaultModel();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        RDFDataMgr.read(model, in, Lang.TURTLE);

        Resource personResource = model.getResource(PERSON_URI);
        String name = personResource.getProperty(VCARD.FN).getString();
        int age = personResource.getProperty(model.createProperty("http://example.org/property/age")).getInt();
        
        Statement emailStatement = personResource.getProperty(VCARD.EMAIL);
        String email = (emailStatement != null) ? emailStatement.getString() : null;

        return (T) new Person(name, age, email);
    }
}