package org.SportsIn.services.strava;

/**
 * Calcule les points SportsIn à attribuer pour une activité Strava importée.
 *
 * Règle MVP :
 *   points = distance en km arrondie, minimum 1 point
 *   bonus  = 1 point supplémentaire si temps > 30 min et dénivelé > 100 m
 */
public class StravaPointCalculator {

    private StravaPointCalculator() {}

    /**
     * @param distanceMeters   distance en mètres (peut être null)
     * @param movingTimeSeconds temps de mouvement en secondes (peut être null)
     * @param elevationGain    dénivelé positif en mètres (peut être null)
     * @return points à attribuer, minimum 1
     */
    public static int calculate(Double distanceMeters, Long movingTimeSeconds, Double elevationGain) {
        int points = 1;

        if (distanceMeters != null && distanceMeters > 0) {
            points = (int) Math.round(distanceMeters / 1000.0);
            if (points < 1) points = 1;
        }

        // Bonus effort : activité longue avec dénivelé
        boolean longActivity = movingTimeSeconds != null && movingTimeSeconds >= 1800; // 30 min
        boolean hasElevation = elevationGain != null && elevationGain >= 100.0;
        if (longActivity && hasElevation) {
            points += 1;
        }

        return points;
    }
}
