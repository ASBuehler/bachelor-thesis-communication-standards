package sqtest;

import com.google.protobuf.InvalidProtocolBufferException;
import sqtest.proto.PersonProto;

public class ProtobufSerializer {

    private PersonProto.Person convertToProtobufPerson(sqtest.Person originalPerson) {
        PersonProto.Person.Builder builder = PersonProto.Person.newBuilder()
                .setName(originalPerson.getName())
                .setAge(originalPerson.getAge());

        if (originalPerson.getEmail() != null) {
            builder.setEmail(originalPerson.getEmail());
        }
        // Wenn die E-Mail null ist, wird der Setter nie aufgerufen. Protobuf verwendet dann
        // den Standardwert für einen String, was ein leerer String "" ist.

        return builder.build();
    }

    public byte[] serialize(sqtest.Person originalPerson) {
        PersonProto.Person protoPerson = convertToProtobufPerson(originalPerson);
        return protoPerson.toByteArray();
    }

    public sqtest.Person deserialize(byte[] data) throws InvalidProtocolBufferException {
        PersonProto.Person protoPerson = PersonProto.Person.parseFrom(data);
        return new sqtest.Person(protoPerson.getName(), protoPerson.getAge(), protoPerson.getEmail());
    }
}