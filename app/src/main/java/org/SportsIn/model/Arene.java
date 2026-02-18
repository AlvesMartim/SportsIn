package org.SportsIn.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.SportsIn.model.user.Equipe;

@Entity
@Table(name = "arene")
public class Arene {

    @Id
    private String id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_controle")
    @JsonBackReference
    private Equipe controllingTeam;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "arene_sport", joinColumns = @JoinColumn(name = "arene_id"))
    @Column(name = "sport_type")
    private List<String> sportsDisponibles;

    // --- Constructeurs ---
    public Arene() {}

    public Arene(String id, String nom, double latitude, double longitude) {
        this.id = id;
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // --- Getters / Setters ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Equipe getControllingTeam() {
        return controllingTeam;
    }

    public void setControllingTeam(Equipe controllingTeam) {
        this.controllingTeam = controllingTeam;
    }

    /**
     * Retourne l'ID de l'équipe qui contrôle cette arène, ou null si neutre.
     * Méthode de convenance pour éviter les NullPointerException.
     */
    public Long getControllingTeamId() {
        return controllingTeam != null ? controllingTeam.getId() : null;
    }

    public List<String> getSportsDisponibles() {
        return sportsDisponibles;
    }

    public void setSportsDisponibles(List<String> sportsDisponibles) {
        this.sportsDisponibles = sportsDisponibles;
    }

    @Override
    public String toString() {
        return "Arene{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
