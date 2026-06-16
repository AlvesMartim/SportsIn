package org.SportsIn.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Représente les données brutes envoyées pour une session de sport.
 * Le moteur se base dessus pour appliquer les règles.
 */
public class SessionResult {

    @JsonIgnore
    private transient Session session;
    private List<MetricValue> metrics;

    private Double weatherHardshipIndex;
    private Double weatherInfluenceBonus;
    private Double weatherAffinityBonus;
    private Double totalInfluenceModifier;
    private String weatherSource;
    private String weatherTags;
    private String weatherSummary;
    private Double weatherTemperatureC;
    private Double weatherWindSpeedMps;
    private Double weatherPrecipitationMm;

    public SessionResult() {}

    public SessionResult(Session session, List<MetricValue> metrics) {
        this.session = session;
        this.metrics = metrics;
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

    public Double getWeatherHardshipIndex() {
        return weatherHardshipIndex;
    }

    public void setWeatherHardshipIndex(Double weatherHardshipIndex) {
        this.weatherHardshipIndex = weatherHardshipIndex;
    }

    public Double getWeatherInfluenceBonus() {
        return weatherInfluenceBonus;
    }

    public void setWeatherInfluenceBonus(Double weatherInfluenceBonus) {
        this.weatherInfluenceBonus = weatherInfluenceBonus;
    }

    public Double getWeatherAffinityBonus() {
        return weatherAffinityBonus;
    }

    public void setWeatherAffinityBonus(Double weatherAffinityBonus) {
        this.weatherAffinityBonus = weatherAffinityBonus;
    }

    public Double getTotalInfluenceModifier() {
        return totalInfluenceModifier;
    }

    public void setTotalInfluenceModifier(Double totalInfluenceModifier) {
        this.totalInfluenceModifier = totalInfluenceModifier;
    }

    public String getWeatherSource() {
        return weatherSource;
    }

    public void setWeatherSource(String weatherSource) {
        this.weatherSource = weatherSource;
    }

    public String getWeatherTags() {
        return weatherTags;
    }

    public void setWeatherTags(String weatherTags) {
        this.weatherTags = weatherTags;
    }

    public String getWeatherSummary() {
        return weatherSummary;
    }

    public void setWeatherSummary(String weatherSummary) {
        this.weatherSummary = weatherSummary;
    }

    public Double getWeatherTemperatureC() {
        return weatherTemperatureC;
    }

    public void setWeatherTemperatureC(Double weatherTemperatureC) {
        this.weatherTemperatureC = weatherTemperatureC;
    }

    public Double getWeatherWindSpeedMps() {
        return weatherWindSpeedMps;
    }

    public void setWeatherWindSpeedMps(Double weatherWindSpeedMps) {
        this.weatherWindSpeedMps = weatherWindSpeedMps;
    }

    public Double getWeatherPrecipitationMm() {
        return weatherPrecipitationMm;
    }

    public void setWeatherPrecipitationMm(Double weatherPrecipitationMm) {
        this.weatherPrecipitationMm = weatherPrecipitationMm;
    }
}
