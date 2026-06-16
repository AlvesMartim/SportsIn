package org.SportsIn.services;

import org.SportsIn.config.StravaProperties;
import org.SportsIn.dto.StravaActivityDTO;
import org.SportsIn.dto.StravaSyncResultDTO;
import org.SportsIn.model.Arene;
import org.SportsIn.model.strava.StravaActivity;
import org.SportsIn.model.territory.Route;
import org.SportsIn.model.territory.RouteRepository;
import org.SportsIn.model.territory.ZoneDepartement;
import org.SportsIn.repository.AreneRepository;
import org.SportsIn.repository.JoueurRepository;
import org.SportsIn.repository.StravaActivityRepository;
import org.SportsIn.repository.ZoneDepartementRepository;
import org.SportsIn.utils.GeoUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Orchestre la synchronisation des activités Strava :
 * 1. Récupération via StravaApiClient
 * 2. Déduplication (strava_activity_id)
 * 3. Anti-cheat (vitesse irréaliste)
 * 4. Décodage polyline → zones/arènes traversées
 * 5. Calcul d'influence et d'XP
 * 6. Persistance
 */
@Service
public class StravaSyncService {

    /** Rayon en km autour d'une arène considéré comme "traversé". */
    private static final double ARENE_PROXIMITY_KM = 0.2;

    /** Vitesses max (m/s) par type d'activité pour l'anti-cheat. */
    private static final Map<String, Double> MAX_AVG_SPEED_MS = new HashMap<>() {{
        put("Run",           7.0);   // 25 km/h
        put("TrailRun",      6.0);
        put("Ride",         22.0);   // 79 km/h
        put("MountainBikeRide", 15.0);
        put("Swim",          2.5);   // 9 km/h
        put("Walk",          3.0);
        put("Hike",          2.5);
        put("WeightTraining", 0.0);  // pas de contrôle vitesse
        put("Yoga",          0.0);
        put("Workout",       0.0);
    }};
    private static final double DEFAULT_MAX_SPEED_MS = 50.0;

    /** XP accordé par km d'activité Strava (sans flag anti-cheat). */
    private static final double XP_PER_KM = 5.0;

    private final StravaApiClient stravaApiClient;
    private final StravaActivityRepository activityRepo;
    private final JoueurRepository joueurRepo;
    private final AreneRepository areneRepo;
    private final RouteRepository routeRepo;
    private final StravaPolylineDecoder polylineDecoder;
    private final XpGrantService xpGrantService;
    private final StravaProperties props;
    private final ApplicationEventPublisher eventPublisher;
    private final ZoneDepartementRepository zoneDeptRepo;

    public StravaSyncService(StravaApiClient stravaApiClient,
                             StravaActivityRepository activityRepo,
                             JoueurRepository joueurRepo,
                             AreneRepository areneRepo,
                             RouteRepository routeRepo,
                             StravaPolylineDecoder polylineDecoder,
                             XpGrantService xpGrantService,
                             StravaProperties props,
                             ApplicationEventPublisher eventPublisher,
                             ZoneDepartementRepository zoneDeptRepo) {
        this.stravaApiClient = stravaApiClient;
        this.activityRepo = activityRepo;
        this.joueurRepo = joueurRepo;
        this.areneRepo = areneRepo;
        this.routeRepo = routeRepo;
        this.polylineDecoder = polylineDecoder;
        this.xpGrantService = xpGrantService;
        this.props = props;
        this.eventPublisher = eventPublisher;
        this.zoneDeptRepo = zoneDeptRepo;
    }

    /**
     * Synchronisation manuelle : récupère les activités récentes du joueur.
     */
    @Transactional
    public StravaSyncResultDTO syncForJoueur(Long joueurId) {
        Long equipeId = resolveEquipeId(joueurId);

        List<StravaActivityDTO> fetched = stravaApiClient.fetchRecentActivities(
                joueurId, props.getMaxActivitiesPerSync());

        return processActivities(fetched, joueurId, equipeId);
    }

    /**
     * Synchronisation d'une activité unique (déclenchée par webhook).
     */
    @Transactional
    public StravaSyncResultDTO syncSingleActivity(Long joueurId, String stravaActivityId) {
        Long equipeId = resolveEquipeId(joueurId);

        StravaActivityDTO dto = stravaApiClient.fetchActivity(joueurId, stravaActivityId);
        return processActivities(List.of(dto), joueurId, equipeId);
    }

    // ----------------------------------------------------------------
    // Traitement interne
    // ----------------------------------------------------------------

    private StravaSyncResultDTO processActivities(List<StravaActivityDTO> activities,
                                                   Long joueurId, Long equipeId) {
        StravaSyncResultDTO result = new StravaSyncResultDTO();
        result.setActivitiesFetched(activities.size());

        List<String> importedIds = new ArrayList<>();
        List<String> flaggedReasons = new ArrayList<>();
        double totalInfluence = 0.0;

        for (StravaActivityDTO dto : activities) {
            String stravaId = String.valueOf(dto.getId());

            // Déduplication
            if (activityRepo.existsByStravaActivityId(stravaId)) {
                result.setActivitiesSkipped(result.getActivitiesSkipped() + 1);
                continue;
            }

            StravaActivity activity = buildActivity(dto, joueurId, equipeId);

            // Anti-cheat
            applyAntiCheatCheck(activity, dto);

            if (!activity.isAntiCheatFlagged()) {
                // Zones/arènes traversées
                List<String> traversedAreneIds = computeTraversedArenes(dto.getSummaryPolyline());
                activity.setZonesTraversed(toJson(traversedAreneIds));

                // Bonus de route
                boolean routeBonus = checkRouteBonus(traversedAreneIds);
                activity.setRouteBonusTriggered(routeBonus);

                // Calcul d'influence accordée
                double influence = computeInfluence(dto, routeBonus);
                activity.setInfluenceGranted(influence);
                totalInfluence += influence;

                // Influence aux zones (départements IDF)
                grantInfluenceToZones(traversedAreneIds, influence);

                // XP à l'équipe
                if (equipeId != null) {
                    int xp = (int) Math.round((dto.getDistance() / 1000.0) * XP_PER_KM);
                    if (xp > 0) xpGrantService.grantActivityXp(equipeId, xp);
                }
            } else {
                result.setActivitiesFlagged(result.getActivitiesFlagged() + 1);
                flaggedReasons.add(stravaId + ": " + activity.getAntiCheatReason());
            }

            activityRepo.save(activity);
            importedIds.add(stravaId);
            result.setActivitiesImported(result.getActivitiesImported() + 1);
        }

        result.setTotalInfluenceGranted(totalInfluence);
        result.setImportedActivityIds(importedIds);
        result.setFlaggedReasons(flaggedReasons);
        return result;
    }

    private StravaActivity buildActivity(StravaActivityDTO dto, Long joueurId, Long equipeId) {
        StravaActivity a = new StravaActivity();
        a.setStravaActivityId(String.valueOf(dto.getId()));
        a.setJoueurId(joueurId);
        a.setEquipeId(equipeId);
        a.setName(dto.getName());
        a.setSportType(dto.getEffectiveSportType());
        a.setDistanceMeters(dto.getDistance());
        a.setMovingTimeSeconds(dto.getMovingTime());
        a.setElapsedTimeSeconds(dto.getElapsedTime());
        a.setTotalElevationGain(dto.getTotalElevationGain());
        a.setAverageSpeed(dto.getAverageSpeed());
        a.setMaxSpeed(dto.getMaxSpeed());
        a.setAverageHeartrate(dto.getAverageHeartrate());
        a.setMaxHeartrate(dto.getMaxHeartrate());
        a.setStartDate(dto.getStartDate() != null ? dto.getStartDate() : Instant.now().toString());
        a.setSummaryPolyline(dto.getSummaryPolyline());
        a.setSyncedAt(Instant.now().toString());
        return a;
    }

    private void applyAntiCheatCheck(StravaActivity activity, StravaActivityDTO dto) {
        String sportType = activity.getSportType();
        double avgSpeed = dto.getAverageSpeed(); // m/s

        double maxAllowed = MAX_AVG_SPEED_MS.getOrDefault(sportType, DEFAULT_MAX_SPEED_MS);

        // maxAllowed == 0 signifie "pas de contrôle vitesse pour ce sport"
        if (maxAllowed > 0 && avgSpeed > maxAllowed) {
            activity.setAntiCheatFlagged(true);
            activity.setAntiCheatReason(String.format(
                    "Vitesse moyenne irréaliste pour %s : %.1f m/s (max %.1f m/s)",
                    sportType, avgSpeed, maxAllowed));
        }

        // Distance minimale pour les activités de déplacement
        if (isMovingActivity(sportType) && dto.getDistance() < 100) {
            activity.setAntiCheatFlagged(true);
            activity.setAntiCheatReason("Distance trop faible : " + dto.getDistance() + " m");
        }
    }

    private boolean isMovingActivity(String sportType) {
        if (sportType == null) return false;
        return switch (sportType) {
            case "WeightTraining", "Yoga", "Workout", "Crossfit",
                 "RockClimbing", "Stretching" -> false;
            default -> true;
        };
    }

    private List<String> computeTraversedArenes(String polyline) {
        if (polyline == null || polyline.isBlank()) return List.of();

        List<StravaPolylineDecoder.LatLng> points = polylineDecoder.decode(polyline);
        List<Arene> allArenes = areneRepo.findAll();

        Set<String> traversed = new HashSet<>();
        for (StravaPolylineDecoder.LatLng pt : points) {
            for (Arene arene : allArenes) {
                double dist = GeoUtils.calculateDistance(
                        pt.lat(), pt.lng(), arene.getLatitude(), arene.getLongitude());
                if (dist <= ARENE_PROXIMITY_KM) {
                    traversed.add(arene.getId());
                }
            }
        }
        return new ArrayList<>(traversed);
    }

    private boolean checkRouteBonus(List<String> traversedAreneIds) {
        if (traversedAreneIds.size() < 2) return false;

        List<Route> routes = routeRepo.findAll();
        Set<String> traversedSet = new HashSet<>(traversedAreneIds);

        for (Route route : routes) {
            List<String> routeAreneIds = route.getArenes().stream()
                    .map(a -> a.getId())
                    .collect(Collectors.toList());

            // La route est "activée" si au moins 2 arènes consécutives sont traversées
            int consecutive = 0;
            for (String areneId : routeAreneIds) {
                if (traversedSet.contains(areneId)) {
                    consecutive++;
                    if (consecutive >= 2) return true;
                } else {
                    consecutive = 0;
                }
            }
        }
        return false;
    }

    private double computeInfluence(StravaActivityDTO dto, boolean routeBonus) {
        double distKm = dto.getDistance() / 1000.0;
        double elevBonus = dto.getTotalElevationGain() / 1000.0; // 1 km de D+ = +1.0
        double base = distKm * 1.0 + elevBonus;

        if (routeBonus) base *= 1.25; // +25% si route activée
        return Math.min(base, 50.0); // Influence max par activité : 50
    }

    private Long resolveEquipeId(Long joueurId) {
        return joueurRepo.findById(joueurId)
                .map(j -> j.getEquipe() != null ? j.getEquipe().getId() : null)
                .orElse(null);
    }

    /**
     * Attribue un bonus d'influence aux zones (départements) dont une arène a été traversée.
     * Chaque zone traversée reçoit 50% de l'influence de l'activité.
     */
    private void grantInfluenceToZones(List<String> traversedAreneIds, double activityInfluence) {
        if (traversedAreneIds.isEmpty()) return;

        Set<String> departements = new HashSet<>();
        for (String areneId : traversedAreneIds) {
            areneRepo.findById(areneId).ifPresent(arene -> {
                if (arene.getDepartement() != null && !arene.getDepartement().isBlank()) {
                    departements.add(arene.getDepartement());
                }
            });
        }

        double bonusParZone = Math.round((activityInfluence * 0.5) * 10.0) / 10.0;
        for (String dept : departements) {
            zoneDeptRepo.findById(dept).ifPresent(zone -> {
                zone.addInfluence(bonusParZone);
                zoneDeptRepo.save(zone);
            });
        }
    }

    /** Sérialise une liste d'IDs en JSON array minimaliste. */
    private String toJson(List<String> ids) {
        if (ids == null || ids.isEmpty()) return "[]";
        return "[\"" + String.join("\",\"", ids) + "\"]";
    }
}
