package main.java.org.SportsIn.model;

/**
 * Représente un sport (ex : Foot, Muscu, Basket 3x3)
 * et les règles associées pour déterminer la victoire et le scoring.
 */
public class Sport {

    private Long id;
    private String code;          // "FOOT", "MUSCU", "BASKET_3X3"
    private String name;          // "Football", "Musculation", ...
    private Long victoryRuleId;   // référence vers une Rule VICTORY
    private Long scoringRuleId;   // référence vers une Rule SCORING

    // --- Constructeurs ---

    public Sport() {
    }

    public Sport(Long id,
                 String code,
                 String name,
                 Long victoryRuleId,
                 Long scoringRuleId) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.victoryRuleId = victoryRuleId;
        this.scoringRuleId = scoringRuleId;
    }

    // --- Getters / Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getVictoryRuleId() {
        return victoryRuleId;
    }

    public void setVictoryRuleId(Long victoryRuleId) {
        this.victoryRuleId = victoryRuleId;
    }

    public Long getScoringRuleId() {
        return scoringRuleId;
    }

    public void setScoringRuleId(Long scoringRuleId) {
        this.scoringRuleId = scoringRuleId;
    }

    // --- Utilitaire ---

    @Override
    public String toString() {
        return "Sport{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", victoryRuleId=" + victoryRuleId +
                ", scoringRuleId=" + scoringRuleId +
                '}';
    }
}
