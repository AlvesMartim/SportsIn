package org.SportsIn.services.strava;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Mappe les types d'activités Strava vers les codes sport SportsIn.
 */
@Component
public class StravaSportMapper {

    private static final Map<String, String> MAPPING = new HashMap<>();

    static {
        // Course
        MAPPING.put("Run", "RUNNING");
        MAPPING.put("TrailRun", "RUNNING");
        MAPPING.put("VirtualRun", "RUNNING");

        // Vélo
        MAPPING.put("Ride", "CYCLING");
        MAPPING.put("MountainBikeRide", "CYCLING");
        MAPPING.put("GravelRide", "CYCLING");
        MAPPING.put("EBikeRide", "CYCLING");
        MAPPING.put("VirtualRide", "CYCLING");

        // Marche / Randonnée
        MAPPING.put("Walk", "WALKING");
        MAPPING.put("Hike", "WALKING");

        // Sports collectifs → sports SportsIn existants
        MAPPING.put("Soccer", "FOOTBALL");
        MAPPING.put("Football", "FOOTBALL");

        // Musculation / fitness
        MAPPING.put("Workout", "MUSCULATION");
        MAPPING.put("WeightTraining", "MUSCULATION");
        MAPPING.put("Crossfit", "MUSCULATION");
        MAPPING.put("Yoga", "MUSCULATION");
        MAPPING.put("Pilates", "MUSCULATION");

        // Tennis
        MAPPING.put("Tennis", "TENNIS");
        MAPPING.put("Squash", "TENNIS");
        MAPPING.put("Badminton", "TENNIS");
        MAPPING.put("Racquetball", "TENNIS");
        MAPPING.put("TableTennis", "TENNIS");

        // Basketball
        MAPPING.put("Basketball", "BASKET");

        // Natation → outdoor
        MAPPING.put("Swim", "OUTDOOR");
        MAPPING.put("OpenWaterSwim", "OUTDOOR");
    }

    /**
     * Retourne le code sport SportsIn correspondant au type Strava.
     * Retourne "OUTDOOR" si le type n'est pas reconnu.
     */
    public String mapToSportsInCode(String stravaType) {
        if (stravaType == null) return "OUTDOOR";
        return MAPPING.getOrDefault(stravaType, "OUTDOOR");
    }
}
