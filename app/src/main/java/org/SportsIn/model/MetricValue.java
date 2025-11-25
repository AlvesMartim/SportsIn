package org.SportsIn.model;

/**
 * Représente une mesure brute enregistrée pendant une session sportive :
 * - pour un participant
 * - d'un type particulier (points, buts, temps...)
 * - avec une valeur numérique
 * - dans un contexte (match1, épreuve2, exercice1...)
 */
public class MetricValue {

    private String participantId;   // id du Participant dans la session
    private MetricType metricType;  // ex: POINTS, GOALS, TIME_SECONDS
    private double value;           // valeur brute
    private String context;         // ex : "match1", "squat", "2km"

    // --- Constructeurs ---
    public MetricValue() {
    }

    public MetricValue(String participantId,
                       MetricType metricType,
                       double value,
                       String context) {
        this.participantId = participantId;
        this.metricType = metricType;
        this.value = value;
        this.context = context;
    }

    // --- Getters / Setters ---

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    // --- Utilitaire ---
    @Override
    public String toString() {
        return "MetricValue{" +
                "participantId='" + participantId + '\'' +
                ", metricType=" + metricType +
                ", value=" + value +
                ", context='" + context + '\'' +
                '}';
    }
}
