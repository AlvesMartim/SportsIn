package org.SportsIn.model.user;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

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

    public Joueur(String pseudo, String email, String password) {
        this.pseudo = pseudo;
        this.email = email;
        this.password = password;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
