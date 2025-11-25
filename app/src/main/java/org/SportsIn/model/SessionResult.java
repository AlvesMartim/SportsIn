package org.SportsIn.model;

import java.util.List;

/**
 * Représente les données brutes envoyées pour une session de sport.
 * Le moteur se base dessus pour appliquer les règles.
 */
public class SessionResult {

    private String sessionId;
    private Long sportId;
    private List<Participant> participants;
    private List<MetricValue> metrics;

    public SessionResult() {}

    public SessionResult(String sessionId,
                         Long sportId,
                         List<Participant> participants,
                         List<MetricValue> metrics) {
        this.sessionId = sessionId;
        this.sportId = sportId;
        this.participants = participants;
        this.metrics = metrics;
    }

    // --- Getters / Setters ---

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getSportId() {
        return sportId;
    }

    public void setSportId(Long sportId) {
        this.sportId = sportId;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public List<MetricValue> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<MetricValue> metrics) {
        this.metrics = metrics;
    }
}
