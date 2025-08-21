package sqtest;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Person")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlPerson {
    private String name;
    private int age;
    private String email;

    public XmlPerson() {}

    public XmlPerson(Person original) {
        this.name = original.getName();
        this.age = original.getAge();
        this.email = original.getEmail();
    }

    public Person toPerson() {
        return new Person(this.name, this.age, this.email);
    }
}