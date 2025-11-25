package org.SportsIn.model;

import java.util.Map;

/**
 * Résultat produit par le moteur après application des règles d’un sport.
 */
public class EvaluationResult {

    private String winnerParticipantId;
    private Map<String, Double> scoresByParticipant; // participantId -> score
    private Map<String, Object> details;             // logs, calculs, debug

    public EvaluationResult() {}

    public EvaluationResult(String winnerParticipantId,
                            Map<String, Double> scoresByParticipant,
                            Map<String, Object> details) {
        this.winnerParticipantId = winnerParticipantId;
        this.scoresByParticipant = scoresByParticipant;
        this.details = details;
    }

    // --- Getters / Setters ---

    public String getWinnerParticipantId() {
        return winnerParticipantId;
    }

    public void setWinnerParticipantId(String winnerParticipantId) {
        this.winnerParticipantId = winnerParticipantId;
    }

    public Map<String, Double> getScoresByParticipant() {
        return scoresByParticipant;
    }

    public void setScoresByParticipant(Map<String, Double> scoresByParticipant) {
        this.scoresByParticipant = scoresByParticipant;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
