package org.SportsIn.model.territory;

/**
 * Représente un bonus actif sur une route pour une équipe donnée.
 */
public class RouteBonus {
    private Long teamId;
    private Route route;
    private int consecutivePoints;
    private String bonusType; // ex: "SCORE_MULTIPLIER", "RECONQUEST_DELAY_REDUCTION"
    private double bonusValue; // ex: 1.10 pour +10%

    public RouteBonus(Long teamId, Route route, int consecutivePoints, String bonusType, double bonusValue) {
        this.teamId = teamId;
        this.route = route;
        this.consecutivePoints = consecutivePoints;
        this.bonusType = bonusType;
        this.bonusValue = bonusValue;
    }

    public Long getTeamId() {
        return teamId;
    }

    public Route getRoute() {
        return route;
    }

    public int getConsecutivePoints() {
        return consecutivePoints;
    }

    public String getBonusType() {
        return bonusType;
    }

    public double getBonusValue() {
        return bonusValue;
    }

    @Override
    public String toString() {
        return "RouteBonus{" +
                "teamId=" + teamId +
                ", route=" + route.getNom() +
                ", consecutivePoints=" + consecutivePoints +
                ", bonusType='" + bonusType + '\'' +
                ", bonusValue=" + bonusValue +
                '}';
    }
}
