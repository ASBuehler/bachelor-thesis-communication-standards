package sqtest;

import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import sqtest.avro.Person; 

import java.io.ByteArrayOutputStream;

public class AvroSerializer { 

    
    private Person convertToAvroPerson(sqtest.Person originalPerson) {
        return Person.newBuilder()
                .setName(originalPerson.getName())
                .setAge(originalPerson.getAge())
                .setEmail(originalPerson.getEmail())
                .build();
    }
    
    
    public byte[] serialize(sqtest.Person originalPerson) throws Exception {
        Person avroPerson = convertToAvroPerson(originalPerson);
        DatumWriter<Person> datumWriter = new SpecificDatumWriter<>(Person.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        datumWriter.write(avroPerson, encoder);
        encoder.flush();
        return out.toByteArray();
    }

    
        public sqtest.Person deserialize(byte[] data) throws Exception {
        DatumReader<Person> datumReader = new SpecificDatumReader<>(Person.class);
        Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        Person avroPerson = datumReader.read(null, decoder);

        
        
        String name = (avroPerson.getName() != null) ? avroPerson.getName().toString() : null;
        String email = (avroPerson.getEmail() != null) ? avroPerson.getEmail().toString() : null;

        return new sqtest.Person(name, avroPerson.getAge(), email);
    }
}