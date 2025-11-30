package org.SportsIn.model;

import java.util.List;

/**
 * Représente les données brutes envoyées pour une session de sport.
 * Le moteur se base dessus pour appliquer les règles.
 */
public class SessionResult {

    private transient Session session;
    private List<MetricValue> metrics;

    public SessionResult() {}

    public SessionResult(Session session, List<MetricValue> metrics) {
        this.session = session;
        this.metrics = metrics;
    }

    public EvaluationResult evaluateRules() {
        if (session != null && session.getSport() != null) {
            return session.getSport().testRule(session);
        }
        return null;
    }

    // --- Getters / Setters ---

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public List<MetricValue> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<MetricValue> metrics) {
        this.metrics = metrics;
    }
}
