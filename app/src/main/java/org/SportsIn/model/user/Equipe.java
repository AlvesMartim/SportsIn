package org.SportsIn.model.user;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Représente une équipe dans le jeu.
 * Une équipe est un groupe de joueurs.
 */
@Entity
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    // Une équipe peut avoir plusieurs joueurs.
    // mappedBy = "equipe" indique que c'est l'entité Joueur qui gère la relation.
    // CascadeType.ALL signifie que les opérations (création, suppression) sur l'équipe
    // se répercutent sur les joueurs associés.
    // FetchType.LAZY est une optimisation pour ne pas charger tous les joueurs inutilement.
    @OneToMany(mappedBy = "equipe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference  // ← Côté "parent" de la relation bidirectionnelle
    private List<Joueur> membres = new ArrayList<>();

    // --- Constructeurs ---

    /**
     * Constructeur par défaut requis par JPA.
     */
    public Equipe() {
    }

    public Equipe(String nom) {
        this.nom = nom;
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

    public List<Joueur> getMembres() {
        return membres;
    }

    public void setMembres(List<Joueur> membres) {
        this.membres = membres;
    }

    // --- Méthodes utilitaires ---

    public void addJoueur(Joueur joueur) {
        membres.add(joueur);
        joueur.setEquipe(this);
    }

    public void removeJoueur(Joueur joueur) {
        membres.remove(joueur);
        joueur.setEquipe(null);
    }

    @Override
    public String toString() {
        return "Equipe{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                '}';
    }
}
