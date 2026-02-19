package org.SportsIn.model.chat;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.SportsIn.model.user.Equipe;
import org.SportsIn.model.user.Joueur;

/**
 * Représente un message de chat d'équipe.
 */
@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "envoye_a", nullable = false)
    private String envoyeA;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "joueur_id", nullable = false)
    private Joueur auteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", nullable = false)
    @JsonBackReference
    private Equipe equipe;

    @Transient
    private Long equipeId;

    public Message() {
    }

    public Message(String contenu, Joueur auteur, Equipe equipe, String envoyeA) {
        this.contenu = contenu;
        this.auteur = auteur;
        this.equipe = equipe;
        this.envoyeA = envoyeA;
    }

    // --- Getters et Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getEnvoyeA() {
        return envoyeA;
    }

    public void setEnvoyeA(String envoyeA) {
        this.envoyeA = envoyeA;
    }

    public Joueur getAuteur() {
        return auteur;
    }

    public void setAuteur(Joueur auteur) {
        this.auteur = auteur;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    public Long getEquipeId() {
        return equipe != null ? equipe.getId() : equipeId;
    }

    public void setEquipeId(Long equipeId) {
        this.equipeId = equipeId;
    }
}
