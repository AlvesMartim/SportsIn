package org.SportsIn.model.territory;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une route sportive, constituée d'une suite ordonnée de points sportifs.
 * Permet de définir des bonus de combo si une équipe contrôle plusieurs points consécutifs.
 */
public class Route {

    private Long id;
    private String nom;
    private String description;
    private List<PointSportif> points;

    public Route() {
        this.points = new ArrayList<>();
    }

    public Route(Long id, String nom, String description, List<PointSportif> points) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.points = points != null ? points : new ArrayList<>();
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PointSportif> getPoints() {
        return points;
    }

    public void setPoints(List<PointSportif> points) {
        this.points = points;
    }

    public void addPoint(PointSportif point) {
        this.points.add(point);
    }

    @Override
    public String toString() {
        return "Route{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", pointsCount=" + (points != null ? points.size() : 0) +
                '}';
    }
}
