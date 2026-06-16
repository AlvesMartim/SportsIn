package org.SportsIn.model.territory;

import jakarta.persistence.*;
import org.SportsIn.model.user.Equipe;

@Entity
@Table(name = "zone_departement")
public class ZoneDepartement {

    @Id
    private String code;

    @Column(nullable = false)
    private String nom;

    @Column(name = "total_influence", nullable = false)
    private double totalInfluence = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controlling_team_id")
    private Equipe controllingTeam;

    public ZoneDepartement() {}

    public ZoneDepartement(String code, String nom) {
        this.code = code;
        this.nom = nom;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getTotalInfluence() { return totalInfluence; }
    public void setTotalInfluence(double totalInfluence) { this.totalInfluence = totalInfluence; }

    public void addInfluence(double amount) { this.totalInfluence += amount; }

    public Equipe getControllingTeam() { return controllingTeam; }
    public void setControllingTeam(Equipe controllingTeam) { this.controllingTeam = controllingTeam; }

    public Long getControllingTeamId() {
        return controllingTeam != null ? controllingTeam.getId() : null;
    }
}
