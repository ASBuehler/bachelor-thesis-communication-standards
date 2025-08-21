package sqtest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PersonAdvanced {

    private String name;
    private int age;
    private String email;
    private String phoneNumber;
    private boolean isActive;
    private LocalDate birthDate;
    private double accountBalance;
    private Address address;
    private List<String> hobbies;
    private List<Dependent> dependents;
    private byte[] profilePicture;
    private Map<String, String> metadata;
    private Status status;

    public PersonAdvanced() {}

    public PersonAdvanced(String name, int age, String email, String phoneNumber, boolean isActive,
                          LocalDate birthDate, double accountBalance, Address address,
                          List<String> hobbies, List<Dependent> dependents,
                          byte[] profilePicture, Map<String, String> metadata, Status status) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.isActive = isActive;
        this.birthDate = birthDate;
        this.accountBalance = accountBalance;
        this.address = address;
        this.hobbies = hobbies;
        this.dependents = dependents;
        this.profilePicture = profilePicture;
        this.metadata = metadata;
        this.status = status;
    }

    // --- GETTER UND SETTER ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public double getAccountBalance() { return accountBalance; }
    public void setAccountBalance(double accountBalance) { this.accountBalance = accountBalance; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public List<String> getHobbies() { return hobbies; }
    public void setHobbies(List<String> hobbies) { this.hobbies = hobbies; }
    public List<Dependent> getDependents() { return dependents; }
    public void setDependents(List<Dependent> dependents) { this.dependents = dependents; }
    public byte[] getProfilePicture() { return profilePicture; }
    public void setProfilePicture(byte[] profilePicture) { this.profilePicture = profilePicture; }
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}