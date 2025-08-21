// src/main/java/sqtest/OrganizationalChartNode.java
package sqtest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OrganizationalChartNode {

    private long id;
    private String name;
    private String role;
    private LocalDate joinedDate;
    private Map<String, String> metadata;
    private List<OrganizationalChartNode> subordinates; 
    
    
    public OrganizationalChartNode() {
        this.subordinates = new ArrayList<>();
    }

    public OrganizationalChartNode(long id, String name, String role, LocalDate joinedDate, Map<String, String> metadata) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.joinedDate = joinedDate;
        this.metadata = metadata;
        this.subordinates = new ArrayList<>();
    }
    
    public void addSubordinate(OrganizationalChartNode node) {
        this.subordinates.add(node);
    }

    // --- Getter und Setter ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDate getJoinedDate() { return joinedDate; }
    public void setJoinedDate(LocalDate joinedDate) { this.joinedDate = joinedDate; }
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    public List<OrganizationalChartNode> getSubordinates() { return subordinates; }
    public void setSubordinates(List<OrganizationalChartNode> subordinates) { this.subordinates = subordinates; }

    // --- equals() und hashCode() für den Stabilitätstest ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationalChartNode that = (OrganizationalChartNode) o;
        
        // Pragmatische, nicht-rekursive Prüfung für den Test.
        // Wir vergleichen die IDs und die Anzahl der direkten Untergebenen.
        // Ein tiefer Vergleich würde bei Zyklen zu einem StackOverflowError führen.
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(role, that.role) &&
                (subordinates != null ? subordinates.size() : 0) == (that.subordinates != null ? that.subordinates.size() : 0);
    }

    @Override
    public int hashCode() {
        // Entsprechend nicht-rekursiver Hashcode.
        return Objects.hash(id, name, role, (subordinates != null ? subordinates.size() : 0));
    }
}