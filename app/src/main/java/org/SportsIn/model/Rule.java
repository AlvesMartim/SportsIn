package org.SportsIn.model;


/**
 * Représente une règle générique, stockée côté back.
 * La logique est décrite dans le champ "definition"
 * (ex : JSON décrivant la règle, qui sera interprété par le moteur).
 */
public class Rule {

    private Long id;
    private String name;
    private RuleType type;      // VICTORY ou SCORING
    private String definition;  // JSON / DSL sérialisée

    // --- Constructeurs ---

    public Rule() {
    }

    public Rule(Long id, String name, RuleType type, String definition) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.definition = definition;
    }

    // --- Getters / Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RuleType getType() {
        return type;
    }

    public void setType(RuleType type) {
        this.type = type;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    // --- Utilitaire ---

    @Override
    public String toString() {
        return "Rule{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
