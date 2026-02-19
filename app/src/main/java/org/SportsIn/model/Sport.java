package org.SportsIn.model;

import jakarta.persistence.*;

/**
 * Représente un sport (ex : Foot, Muscu, Basket)
 * et les références vers les règles de victoire et de scoring.
 */
@Entity
@Table(name = "sport")
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;          // "FOOTBALL", "BASKET", "TENNIS", "MUSCULATION"

    @Column(nullable = false)
    private String name;          // "Football", "Musculation", ...

    @Column(name = "scoring_rule_id")
    private Long scoringRuleId;   // référence vers une Rule SCORING

    @Column(name = "victory_rule_id")
    private Long victoryRuleId;

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
