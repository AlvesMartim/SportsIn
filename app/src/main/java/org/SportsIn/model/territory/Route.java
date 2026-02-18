package org.SportsIn.model.territory;

import org.SportsIn.model.Arene;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une route sportive, constituée d'une suite ordonnée d'arènes.
 * Permet de définir des bonus de combo si une équipe contrôle plusieurs arènes consécutives.
 */
public class Route {

    private Long id;
    private String nom;
    private String description;
    private List<Arene> arenes;

    public Route() {
        this.arenes = new ArrayList<>();
    }

    public Route(Long id, String nom, String description, List<Arene> arenes) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.arenes = arenes != null ? arenes : new ArrayList<>();
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

    public List<Arene> getArenes() {
        return arenes;
    }

    public void setArenes(List<Arene> arenes) {
        this.arenes = arenes;
    }

    public void addArene(Arene arene) {
        this.arenes.add(arene);
    }

    @Override
    public String toString() {
        return "Route{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", arenesCount=" + (arenes != null ? arenes.size() : 0) +
                '}';
    }
}
