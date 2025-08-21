package sqtest;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TTransport;
import sqtest.thrift.Person; 

public class ThriftSerializer { // Implementiert nicht das Interface

    // Konvertiert unser Test-Objekt in ein Thrift-Objekt
    private Person convertToThriftPerson(sqtest.Person originalPerson) {
        Person thriftPerson = new Person();
        thriftPerson.setName(originalPerson.getName());
        thriftPerson.setAge(originalPerson.getAge());

        // Der Setter wird nur aufgerufen, wenn der Wert nicht null ist.
        if (originalPerson.getEmail() != null) {
            thriftPerson.setEmail(originalPerson.getEmail());
        }
        return thriftPerson;
    }

    public byte[] serialize(sqtest.Person originalPerson) throws TException {
        Person thriftPerson = convertToThriftPerson(originalPerson);
        TMemoryBuffer transport = new TMemoryBuffer(1024);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        thriftPerson.write(protocol);
        return transport.getArray();
    }

    public sqtest.Person deserialize(byte[] data) throws TException {
        TMemoryBuffer transport = new TMemoryBuffer(data.length);
        transport.write(data);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        Person thriftPerson = new Person();
        thriftPerson.read(protocol);

        
        // Prüft mit isSet<Feldname>(), ob das Feld im serialisierten Objekt vorhanden war.
        String email = thriftPerson.isSetEmail() ? thriftPerson.getEmail() : null;

        return new sqtest.Person(thriftPerson.getName(), thriftPerson.getAge(), email);
    }
}