package kketest.model;

public class DataObject {
    private String data; 

    // Leerer Konstruktor ist wichtig für Jackson
    public DataObject() {}

    public DataObject(String data) {
        this.data = data;
    }

    // Getter und Setter an den neuen Namen anpassen
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}