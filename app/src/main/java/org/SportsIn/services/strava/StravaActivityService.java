package org.SportsIn.services.strava;

import org.SportsIn.model.strava.JoueurStravaData;
import org.SportsIn.model.strava.StravaActivity;
import org.SportsIn.repository.JoueurStravaDataRepository;
import org.SportsIn.repository.StravaActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Récupère les activités depuis l'API Strava et les persiste localement.
 */
@Service
public class StravaActivityService {

    private static final Logger log = LoggerFactory.getLogger(StravaActivityService.class);
    private static final int DEFAULT_ACTIVITIES_PER_PAGE = 5;

    private final StravaApiClient apiClient;
    private final StravaOAuthService oAuthService;
    private final StravaActivityRepository activityRepository;
    private final JoueurStravaDataRepository stravaDataRepository;
    private final StravaSportMapper sportMapper;

    public StravaActivityService(StravaApiClient apiClient,
                                 StravaOAuthService oAuthService,
                                 StravaActivityRepository activityRepository,
                                 JoueurStravaDataRepository stravaDataRepository,
                                 StravaSportMapper sportMapper) {
        this.apiClient = apiClient;
        this.oAuthService = oAuthService;
        this.activityRepository = activityRepository;
        this.stravaDataRepository = stravaDataRepository;
        this.sportMapper = sportMapper;
    }

    /**
     * Récupère les dernières activités Strava d'un joueur et sauvegarde
     * uniquement celles qui ne sont pas encore en base.
     *
     * @return liste des nouvelles activités persistées
     */
    @Transactional
    public List<StravaActivity> fetchAndSaveLatestActivities(Long joueurId) {
        JoueurStravaData stravaData = getConnectedStravaData(joueurId);
        oAuthService.refreshIfNeeded(stravaData);

        List<Map<String, Object>> rawActivities =
            apiClient.getAthleteActivities(stravaData.getStravaAccessToken(), DEFAULT_ACTIVITIES_PER_PAGE);

        List<StravaActivity> newActivities = new ArrayList<>();
        for (Map<String, Object> raw : rawActivities) {
            Long stravaId = extractLong(raw, "id");
            if (stravaId == null) continue;

            if (!activityRepository.existsByJoueurIdAndStravaActivityId(joueurId, stravaId)) {
                StravaActivity activity = mapToEntity(joueurId, raw);
                newActivities.add(activityRepository.save(activity));
                log.info("Nouvelle activité Strava sauvegardée : {} (joueur {})", stravaId, joueurId);
            }
        }
        return newActivities;
    }

    /**
     * Récupère une activité précise par son ID Strava et la sauvegarde si elle n'existe pas.
     */
    @Transactional
    public StravaActivity fetchAndSaveActivityById(Long joueurId, Long stravaActivityId) {
        return activityRepository.findByJoueurIdAndStravaActivityId(joueurId, stravaActivityId)
            .orElseGet(() -> {
                JoueurStravaData stravaData = getConnectedStravaData(joueurId);
                oAuthService.refreshIfNeeded(stravaData);
                Map<String, Object> raw = apiClient.getActivityById(stravaData.getStravaAccessToken(), stravaActivityId);
                StravaActivity activity = mapToEntity(joueurId, raw);
                return activityRepository.save(activity);
            });
    }

    /**
     * Retourne toutes les activités Strava persistées pour un joueur.
     */
    public List<StravaActivity> getActivitiesForJoueur(Long joueurId) {
        return activityRepository.findByJoueurIdOrderByStartDateDesc(joueurId);
    }

    JoueurStravaData getConnectedStravaData(Long joueurId) {
        return stravaDataRepository.findByJoueurId(joueurId)
            .filter(JoueurStravaData::isStravaConnected)
            .orElseThrow(() -> new IllegalStateException(
                "Le joueur " + joueurId + " n'est pas connecté à Strava"));
    }

    private StravaActivity mapToEntity(Long joueurId, Map<String, Object> raw) {
        StravaActivity activity = new StravaActivity();
        activity.setJoueurId(joueurId);
        activity.setStravaActivityId(extractLong(raw, "id"));

        // Strava API v3 utilise "sport_type", avec "type" en fallback (legacy)
        String type = extractString(raw, "sport_type");
        if (type == null) type = extractString(raw, "type");
        activity.setStravaType(type);
        activity.setSportCode(sportMapper.mapToSportsInCode(type));

        activity.setName(extractString(raw, "name"));
        activity.setStartDate(extractString(raw, "start_date"));
        activity.setDistanceMeters(extractDouble(raw, "distance"));
        activity.setMovingTimeSeconds(extractLong(raw, "moving_time"));
        activity.setElevationGain(extractDouble(raw, "total_elevation_gain"));

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) raw.get("map");
        if (map != null) {
            activity.setPolylineEncoded(extractString(map, "summary_polyline"));
        }

        activity.setCreatedAt(Instant.now().toString());
        return activity;
    }

    private Long extractLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val instanceof Number n ? n.longValue() : null;
    }

    private Double extractDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val instanceof Number n ? n.doubleValue() : null;
    }

    private String extractString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val instanceof String s ? s : null;
    }
}
