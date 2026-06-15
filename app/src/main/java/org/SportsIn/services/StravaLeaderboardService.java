package org.SportsIn.services;

import org.SportsIn.model.strava.StravaActivity;
import org.SportsIn.model.territory.Zone;
import org.SportsIn.model.territory.ZoneRepository;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.repository.StravaActivityRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calcule les classements Strava avec :
 * - filtre par période (7j / 30j)
 * - filtre optionnel par zone géographique
 * - décomposition par sport (Walk, Run, Ride, etc.)
 */
@Service
public class StravaLeaderboardService {

    private static final Map<String, String> SPORT_LABELS = Map.of(
            "Run",           "Course",
            "TrailRun",      "Trail",
            "Ride",          "Vélo",
            "MountainBikeRide", "VTT",
            "Walk",          "Marche",
            "Hike",          "Randonnée",
            "WeightTraining","Muscu",
            "Swim",          "Natation",
            "Workout",       "Entraînement"
    );

    private final StravaActivityRepository activityRepo;
    private final ZoneRepository zoneRepo;
    private final EquipeRepository equipeRepo;

    public StravaLeaderboardService(StravaActivityRepository activityRepo,
                                    ZoneRepository zoneRepo,
                                    EquipeRepository equipeRepo) {
        this.activityRepo = activityRepo;
        this.zoneRepo = zoneRepo;
        this.equipeRepo = equipeRepo;
    }

    /**
     * Calcule le classement des équipes.
     *
     * @param period   "weekly" (7j) ou "monthly" (30j)
     * @param zoneId   si non null, filtre les activités qui traversent cette zone
     * @return liste ordonnée par distance décroissante
     */
    public List<Map<String, Object>> compute(String period, Long zoneId) {
        String sinceDate = sinceDate(period);

        // Récupérer toutes les activités valides de la période
        List<StravaActivity> activities = activityRepo.findValidSince(sinceDate);

        // Filtrer par zone si demandé
        if (zoneId != null) {
            activities = filterByZone(activities, zoneId);
        }

        // Grouper par équipe
        Map<Long, List<StravaActivity>> byEquipe = activities.stream()
                .filter(a -> a.getEquipeId() != null)
                .collect(Collectors.groupingBy(StravaActivity::getEquipeId));

        // Construire les entrées du classement
        List<Map<String, Object>> entries = new ArrayList<>();
        for (Map.Entry<Long, List<StravaActivity>> entry : byEquipe.entrySet()) {
            Long equipeId = entry.getKey();
            List<StravaActivity> acts = entry.getValue();

            double totalDistance = acts.stream().mapToDouble(StravaActivity::getDistanceMeters).sum();
            long totalCount = acts.size();

            // Décomposition par sport
            Map<String, Map<String, Object>> sportBreakdown = new LinkedHashMap<>();
            acts.stream()
                .collect(Collectors.groupingBy(StravaActivity::getSportType))
                .forEach((sport, sportActs) -> {
                    double dist = sportActs.stream().mapToDouble(StravaActivity::getDistanceMeters).sum();
                    Map<String, Object> sportEntry = new LinkedHashMap<>();
                    sportEntry.put("sport", sport);
                    sportEntry.put("label", SPORT_LABELS.getOrDefault(sport, sport));
                    sportEntry.put("distanceKm", Math.round(dist / 10.0) / 100.0);
                    sportEntry.put("count", sportActs.size());
                    sportBreakdown.put(sport, sportEntry);
                });

            Map<String, Object> leaderEntry = new LinkedHashMap<>();
            leaderEntry.put("equipeId", equipeId);
            leaderEntry.put("distanceKm", Math.round(totalDistance / 10.0) / 100.0);
            leaderEntry.put("activitiesCount", totalCount);
            leaderEntry.put("sportBreakdown", new ArrayList<>(sportBreakdown.values()));
            equipeRepo.findById(equipeId).ifPresent(e -> leaderEntry.put("equipeName", e.getNom()));

            entries.add(leaderEntry);
        }

        // Trier par distance décroissante et ajouter le rang
        entries.sort((a, b) -> Double.compare(
                (double) b.get("distanceKm"), (double) a.get("distanceKm")));

        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).put("rank", i + 1);
        }

        return entries;
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private List<StravaActivity> filterByZone(List<StravaActivity> activities, Long zoneId) {
        Optional<Zone> zoneOpt = zoneRepo.findById(zoneId);
        if (zoneOpt.isEmpty()) return List.of();

        // Récupérer les IDs d'arènes appartenant à cette zone
        Set<String> areneIds = zoneOpt.get().getArenes().stream()
                .map(a -> a.getId())
                .collect(Collectors.toSet());

        return activities.stream()
                .filter(a -> a.getZonesTraversed() != null
                        && areneIds.stream().anyMatch(
                                id -> a.getZonesTraversed().contains("\"" + id + "\"")))
                .collect(Collectors.toList());
    }

    private String sinceDate(String period) {
        long days = "monthly".equals(period) ? 30L : 7L;
        return Instant.now().minus(days, ChronoUnit.DAYS).toString();
    }
}
