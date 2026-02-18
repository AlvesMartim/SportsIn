package org.SportsIn.services;

import org.SportsIn.model.Arene;
import org.SportsIn.model.territory.Route;
import org.SportsIn.model.territory.RouteBonus;

import java.util.ArrayList;
import java.util.List;

/**
 * Service gérant les routes sportives et le calcul des bonus de combo.
 */
public class RouteService {

    // Seuil minimal d'arènes consécutives pour déclencher un bonus
    private static final int MIN_CONSECUTIVE_ARENES_FOR_BONUS = 3;

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
            int maxConsecutive = getMaxConsecutiveArenes(route, teamId);

            if (maxConsecutive >= MIN_CONSECUTIVE_ARENES_FOR_BONUS) {
                double bonusValue = 0.10; 
                bonuses.add(new RouteBonus(teamId, route, maxConsecutive, "SCORE_MULTIPLIER", bonusValue));
            }
        }

        return bonuses;
    }

    /**
     * Détermine le nombre maximum d'arènes consécutives contrôlées par une équipe sur une route.
     *
     * @param route La route à analyser.
     * @param teamId L'ID de l'équipe.
     * @return Le nombre maximum d'arènes consécutives.
     */
    public int getMaxConsecutiveArenes(Route route, Long teamId) {
        List<Arene> arenes = route.getArenes();
        if (arenes == null || arenes.isEmpty()) {
            return 0;
        }

        int maxConsecutive = 0;
        int currentConsecutive = 0;

        for (Arene arene : arenes) {
            if (teamId.equals(arene.getControllingTeamId())) {
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
