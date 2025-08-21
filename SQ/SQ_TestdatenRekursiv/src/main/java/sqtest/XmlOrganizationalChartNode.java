package sqtest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dies ist eine Data Transfer Object (DTO) Klasse, die speziell für die 
 * JAXB-Serialisierung von OrganizationalChartNode entwickelt wurde.
 * Sie enthält die notwendigen JAXB-Annotationen und löst die Rekursion auf,
 * indem sie eine saubere, annotierte Struktur für XML bereitstellt.
 * Sie isoliert die XML-spezifischen Anforderungen vom eigentlichen Domain-Modell.
 */
@XmlRootElement(name = "node") // Das Wurzelelement wird <node> heißen
@XmlAccessorType(XmlAccessType.FIELD) // JAXB greift direkt auf die Felder zu
public class XmlOrganizationalChartNode {

    private long id;
    private String name;
    private String role;

    // Wir benötigen einen Adapter, um LocalDate in einen String für XML zu konvertieren
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate joinedDate;

    // Das Map-Feld 'metadata' wird bewusst weggelassen, da es einen komplexeren
    // MapAdapter erfordern würde und der Fokus dieses Tests auf der Rekursion liegt.
    
    // Dies ist die entscheidende Annotation für die rekursive Liste.
    // Es wird eine <subordinates>-Hülle um die einzelnen <node>-Elemente geben.
    @XmlElementWrapper(name = "subordinates")
    @XmlElement(name = "node")
    private List<XmlOrganizationalChartNode> subordinates;

    // Ein Standardkonstruktor ist für JAXB erforderlich.
    public XmlOrganizationalChartNode() {
        this.subordinates = new ArrayList<>();
    }

    // --- GETTER UND SETTER ---
    // Sie werden für die Konvertierungslogik im XmlSerializer benötigt.

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDate joinedDate) {
        this.joinedDate = joinedDate;
    }

    public List<XmlOrganizationalChartNode> getSubordinates() {
        return subordinates;
    }

    public void setSubordinates(List<XmlOrganizationalChartNode> subordinates) {
        this.subordinates = subordinates;
    }
}