package org.SportsIn.model.territory;

import org.SportsIn.model.Sport;
import java.util.List;

/**
 * Représente un point d'intérêt sportif sur la carte.
 * Version simplifiée pour la gestion du contrôle direct.
 */
public class PointSportif {

    private Long id;
    private String nom;
    private double latitude;
    private double longitude;

    // Liste des sports qu'on peut pratiquer sur ce point.
    private List<Sport> sportsDisponibles;

    // Contient l'ID de l'équipe qui contrôle le point.
    // Si la valeur est null, le point est considéré comme neutre.
    private Long controllingTeamId;

    // --- Constructeurs ---

    public PointSportif() {}

    /**
     * Constructeur pour créer un nouveau point sportif.
     * @param id L'identifiant unique du point.
     * @param nom Le nom affiché du point (ex: "City Stade de la Villette").
     * @param latitude La latitude géographique.
     * @param longitude La longitude géographique.
     * @param sports La liste des sports praticables.
     */
    public PointSportif(Long id, String nom, double latitude, double longitude, List<Sport> sports) {
        this.id = id;
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sportsDisponibles = sports;
        this.controllingTeamId = null; // Un point est toujours neutre à sa création.
    }

    // --- Getters et Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public List<Sport> getSportsDisponibles() {
        return sportsDisponibles;
    }

    public void setSportsDisponibles(List<Sport> sportsDisponibles) {
        this.sportsDisponibles = sportsDisponibles;
    }

    public Long getControllingTeamId() {
        return controllingTeamId;
    }

    public void setControllingTeamId(Long controllingTeamId) {
        this.controllingTeamId = controllingTeamId;
    }

    @Override
    public String toString() {
        return "PointSportif{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", controllingTeamId=" + controllingTeamId +
                '}';
    }
}
