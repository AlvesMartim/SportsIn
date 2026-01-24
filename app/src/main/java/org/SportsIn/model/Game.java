package org.SportsIn.model;

import org.SportsIn.model.user.Equipe;

import java.time.LocalDateTime;

/**
 * Représente un jeu/match entre deux équipes.
 * Un Game est créé quand une équipe lance un défi,
 * puis une autre équipe peut rejoindre pour créer le match.
 */
public class Game {

    private String id;
    private Sport sport;
    private String pointId;           // Arène/point où se déroule le jeu
    private Equipe creatorTeam;       // Équipe qui a créé le jeu
    private Equipe opponentTeam;      // Équipe adverse (null si en attente)
    private GameState state;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String sessionId;         // Lien vers la session quand le jeu démarre
    private String winnerTeamId;      // ID de l'équipe gagnante

    public Game() {
        this.state = GameState.WAITING;
        this.createdAt = LocalDateTime.now();
    }

    public Game(String id, Sport sport, String pointId, Equipe creatorTeam) {
        this.id = id;
        this.sport = sport;
        this.pointId = pointId;
        this.creatorTeam = creatorTeam;
        this.state = GameState.WAITING;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters / Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Sport getSport() {
        return sport;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public Equipe getCreatorTeam() {
        return creatorTeam;
    }

    public void setCreatorTeam(Equipe creatorTeam) {
        this.creatorTeam = creatorTeam;
    }

    public Equipe getOpponentTeam() {
        return opponentTeam;
    }

    public void setOpponentTeam(Equipe opponentTeam) {
        this.opponentTeam = opponentTeam;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getWinnerTeamId() {
        return winnerTeamId;
    }

    public void setWinnerTeamId(String winnerTeamId) {
        this.winnerTeamId = winnerTeamId;
    }

    @Override
    public String toString() {
        return "Game{" +
                "id='" + id + '\'' +
                ", sport=" + sport +
                ", pointId='" + pointId + '\'' +
                ", creatorTeam=" + (creatorTeam != null ? creatorTeam.getNom() : "null") +
                ", opponentTeam=" + (opponentTeam != null ? opponentTeam.getNom() : "null") +
                ", state=" + state +
                '}';
    }
}
