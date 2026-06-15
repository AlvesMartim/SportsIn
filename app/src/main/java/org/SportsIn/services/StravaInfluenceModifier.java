package org.SportsIn.services;

import org.SportsIn.model.strava.StravaActivity;
import org.SportsIn.repository.StravaActivityRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Modificateur d'influence basé sur les activités Strava récentes de l'équipe.
 * Ordre 30 (après Route=10 et Perk=20) dans la Chain of Responsibility de l'InfluenceCalculator.
 *
 * Logique :
 *  - Récupère les activités Strava non-flaggées de l'équipe des 7 derniers jours
 *  - Pour chaque activité qui mentionne l'arène (pointId) dans zones_traversed :
 *      bonus += influence_granted * FACTOR
 *  - Le bonus total est plafonné à 0.40 (40% d'influence supplémentaire)
 */
@Component
public class StravaInfluenceModifier implements InfluenceModifier {

    private static final double FACTOR = 0.02;
    private static final double MAX_BONUS = 0.40;
    private static final int LOOKBACK_DAYS = 7;

    private final StravaActivityRepository activityRepo;

    public StravaInfluenceModifier(StravaActivityRepository activityRepo) {
        this.activityRepo = activityRepo;
    }

    @Override
    public double apply(Long teamId, String pointId, double currentModifier) {
        if (teamId == null || pointId == null) return currentModifier;

        String sinceDate = Instant.now().minus(LOOKBACK_DAYS, ChronoUnit.DAYS).toString();
        List<StravaActivity> recentActivities = activityRepo.findValidByEquipeSince(teamId, sinceDate);

        double bonus = 0.0;
        for (StravaActivity activity : recentActivities) {
            if (activityTraversedArene(activity, pointId)) {
                bonus += activity.getInfluenceGranted() * FACTOR;
            }
        }

        bonus = Math.min(bonus, MAX_BONUS);
        return currentModifier + bonus;
    }

    @Override
    public int getOrder() {
        return 30;
    }

    /** Vérifie si pointId apparaît dans le JSON array zones_traversed de l'activité. */
    private boolean activityTraversedArene(StravaActivity activity, String pointId) {
        String zones = activity.getZonesTraversed();
        return zones != null && zones.contains("\"" + pointId + "\"");
    }
}
