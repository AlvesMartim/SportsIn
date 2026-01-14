package org.SportsIn.model.user;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Représente un joueur (un utilisateur individuel).
 */
@Entity
public class Joueur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String pseudo;

    // Plusieurs joueurs peuvent appartenir à la même équipe.
    // @JoinColumn spécifie la colonne de la base de données (dans la table Joueur)
    // qui stockera la clé étrangère vers l'équipe.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id")
    @JsonBackReference  // ← Côté "enfant" de la relation bidirectionnelle
    private Equipe equipe;

    // --- Constructeurs ---

    /**
     * Constructeur par défaut requis par JPA.
     */
    public Joueur() {
    }



    public Joueur(String pseudo) {
        this.pseudo = pseudo;
    }

    // --- Getters et Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    @Override
    public String toString() {
        return "Joueur{" +
                "id=" + id +
                ", pseudo='" + pseudo + '\'' +
                ", equipe=" + (equipe != null ? equipe.getNom() : "aucune") +
                '}';
    }
}
