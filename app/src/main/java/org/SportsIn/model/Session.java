package org.SportsIn.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Représente une session de sport (Foot, Muscu, Basket...).
 * Une session est créée sur un point, possède des participants,
 * et produit un résultat une fois terminée.
 */

/* TODO:Il faut que ça crée un Sport selon le sport id, il faut que ça crée un sessionResult  */
public class Session {

    private String id;                       // identifiant unique "SESSION_42"
    private Long sportId;                    // référence vers Sport
    private String pointId;                  // identifiant du point sur la carte
    private SessionState state;              // ACTIVE / TERMINATED
    private LocalDateTime createdAt;         // date de création
    private LocalDateTime endedAt;           // date de fin si terminée

    private List<Participant> participants;  // joueurs / équipes
    private SessionResult result;            // données brutes envoyées (scores)

    private String winnerParticipantId;      // id du gagnant une fois règles appliquées

    // --- Constructeurs ---

    public Session() {}

    public Session(String id,
                   Long sportId,
                   String pointId,
                   SessionState state,
                   LocalDateTime createdAt,
                   List<Participant> participants) {

        this.id = id;
        this.sportId = sportId;
        this.pointId = pointId;
        this.state = state;
        this.createdAt = createdAt;
        this.participants = participants;
    }

    // --- Getters / Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getSportId() {
        return sportId;
    }

    public void setSportId(Long sportId) {
        this.sportId = sportId;
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public SessionState getState() {
        return state;
    }

    public void setState(SessionState state) {
        this.state = state;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public SessionResult getResult() {
        return result;
    }

    public void setResult(SessionResult result) {
        this.result = result;
    }

    public String getWinnerParticipantId() {
        return winnerParticipantId;
    }

    public void setWinnerParticipantId(String winnerParticipantId) {
        this.winnerParticipantId = winnerParticipantId;
    }

    // --- Utilitaires ---

    @Override
    public String toString() {
        return "Session{" +
                "id='" + id + '\'' +
                ", sportId=" + sportId +
                ", pointId='" + pointId + '\'' +
                ", state=" + state +
                ", createdAt=" + createdAt +
                ", endedAt=" + endedAt +
                '}';
    }
}
