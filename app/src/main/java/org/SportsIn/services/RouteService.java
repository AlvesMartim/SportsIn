package org.SportsIn.services;

import org.SportsIn.model.territory.PointSportif;
import org.SportsIn.model.territory.Route;
import org.SportsIn.model.territory.RouteBonus;

import java.util.ArrayList;
import java.util.List;

/**
 * Service gérant les routes sportives et le calcul des bonus de combo.
 */
public class RouteService {

    // Seuil minimal de points consécutifs pour déclencher un bonus
    private static final int MIN_CONSECUTIVE_POINTS_FOR_BONUS = 3;

    /**
     * Calcule les bonus actifs pour une équipe donnée sur une liste de routes.
     *
     * @param routes Liste des routes à analyser.
     * @param teamId ID de l'équipe pour laquelle on calcule les bonus.
     * @return Liste des bonus débloqués.
     */
    public List<RouteBonus> calculateBonuses(List<Route> routes, Long teamId) {
        List<RouteBonus> bonuses = new ArrayList<>();

        for (Route route : routes) {
            int maxConsecutive = getMaxConsecutivePoints(route, teamId);

            if (maxConsecutive >= MIN_CONSECUTIVE_POINTS_FOR_BONUS) {
                // Exemple de règle : 10% de bonus si >= 3 points
                // On pourrait complexifier la règle ici ou la sortir dans une stratégie
                double bonusValue = 0.10; 
                bonuses.add(new RouteBonus(teamId, route, maxConsecutive, "SCORE_MULTIPLIER", bonusValue));
            }
        }

        return bonuses;
    }

    /**
     * Détermine le nombre maximum de points consécutifs contrôlés par une équipe sur une route.
     *
     * @param route La route à analyser.
     * @param teamId L'ID de l'équipe.
     * @return Le nombre maximum de points consécutifs.
     */
    public int getMaxConsecutivePoints(Route route, Long teamId) {
        List<PointSportif> points = route.getPoints();
        if (points == null || points.isEmpty()) {
            return 0;
        }

        int maxConsecutive = 0;
        int currentConsecutive = 0;

        for (PointSportif point : points) {
            if (teamId.equals(point.getControllingTeamId())) {
                currentConsecutive++;
            } else {
                if (currentConsecutive > maxConsecutive) {
                    maxConsecutive = currentConsecutive;
                }
                currentConsecutive = 0;
            }
        }
        // Check final sequence
        if (currentConsecutive > maxConsecutive) {
            maxConsecutive = currentConsecutive;
        }

        return maxConsecutive;
    }
}
