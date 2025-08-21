package sqtest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// Diese Klasse ist NUR für die XML-Serialisierung da.
@XmlRootElement(name = "PersonAdvanced") // Wichtig für einen sauberen Root-Namen im XML
@XmlAccessorType(XmlAccessType.FIELD) // Sagt JAXB, es soll die Felder verwenden
public class XmlPersonAdvanced {

    // Felder sind exakte Kopien von PersonAdvanced
    private String name;
    private int age;
    private String email;
    private String phoneNumber;
    private boolean isActive;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate birthDate;

    private double accountBalance;
    private Address address;
    private List<String> hobbies;
    private List<Dependent> dependents;
    private byte[] profilePicture;

    @XmlJavaTypeAdapter(MapAdapter.class)
    private Map<String, String> metadata;

    @XmlJavaTypeAdapter(StatusAdapter.class)
    private Status status;

    // Leerer Konstruktor ist für JAXB notwendig
    public XmlPersonAdvanced() {}

    // Konstruktor, der ein PersonAdvanced-Objekt entgegennimmt und die Werte kopiert
    public XmlPersonAdvanced(PersonAdvanced original) {
        this.name = original.getName();
        this.age = original.getAge();
        this.email = original.getEmail();
        this.phoneNumber = original.getPhoneNumber();
        this.isActive = original.isActive();
        this.birthDate = original.getBirthDate();
        this.accountBalance = original.getAccountBalance();
        this.address = original.getAddress(); // Funktioniert, da Address/Dependent keine komplexen Adapter brauchen
        this.hobbies = original.getHobbies();
        this.dependents = original.getDependents();
        this.profilePicture = original.getProfilePicture();
        this.metadata = original.getMetadata();
        this.status = original.getStatus();
    }

    // Methode, um das Objekt zurück in ein PersonAdvanced-Objekt zu konvertieren
    public PersonAdvanced toPersonAdvanced() {
        return new PersonAdvanced(
            this.name, this.age, this.email, this.phoneNumber, this.isActive,
            this.birthDate, this.accountBalance, this.address, this.hobbies,
            this.dependents, this.profilePicture, this.metadata, this.status
        );
    }
}