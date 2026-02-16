package org.SportsIn.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.SportsIn.model.Session;
import org.SportsIn.model.SessionRepository;
import org.SportsIn.model.SessionState;
import org.SportsIn.model.mission.*;
import org.SportsIn.model.territory.*;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.repository.MissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Évalue les missions actives et gère les transitions de statut,
 * l'attribution de récompenses, et l'expiration.
 */
@Service
public class MissionEvaluationService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final MissionRepository missionRepository;
    private final EquipeRepository equipeRepository;
    private final PointSportifRepository pointRepository;
    private final RouteRepository routeRepository;
    private final SessionRepository sessionRepository;
    private final TerritoryService territoryService;

    public MissionEvaluationService(MissionRepository missionRepository,
                                    EquipeRepository equipeRepository,
                                    PointSportifRepository pointRepository,
                                    RouteRepository routeRepository,
                                    SessionRepository sessionRepository,
                                    TerritoryService territoryService) {
        this.missionRepository = missionRepository;
        this.equipeRepository = equipeRepository;
        this.pointRepository = pointRepository;
        this.routeRepository = routeRepository;
        this.sessionRepository = sessionRepository;
        this.territoryService = territoryService;
    }

    /**
     * Évalue une mission spécifique et met à jour son statut.
     */
    @Transactional
    public Mission evaluateMission(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission introuvable: " + missionId));

        if (mission.getStatus() != MissionStatus.ACTIVE) {
            return mission;
        }

        // Vérifier expiration d'abord
        if (mission.isExpired()) {
            mission.setStatus(MissionStatus.EXPIRED);
            mission.setLastEvaluatedAt(Instant.now().toString());
            return missionRepository.save(mission);
        }

        Map<String, Object> payload = parsePayload(mission.getPayloadJson());
        boolean success = evaluateByType(mission, payload);

        if (success) {
            completeMissionSuccess(mission);
        }

        mission.setLastEvaluatedAt(Instant.now().toString());
        return missionRepository.save(mission);
    }

    /**
     * Évalue toutes les missions actives d'une équipe.
     */
    @Transactional
    public void evaluateActiveMissionsForTeam(Long teamId) {
        List<Mission> activeMissions = missionRepository.findActiveByTeam(teamId);
        for (Mission m : activeMissions) {
            evaluateMission(m.getId());
        }
    }

    /**
     * Expire toutes les missions actives dont la date de fin est dépassée.
     */
    @Transactional
    public void expireActiveMissions() {
        String now = Instant.now().toString();
        List<Mission> expired = missionRepository.findActiveEndingBefore(now);
        for (Mission m : expired) {
            m.setStatus(MissionStatus.EXPIRED);
            m.setLastEvaluatedAt(now);
            missionRepository.save(m);
        }
    }

    /**
     * Évalue toutes les missions actives (toutes équipes).
     */
    @Transactional
    public void evaluateAllActiveMissions() {
        List<Mission> allActive = missionRepository.findAllActive();
        for (Mission m : allActive) {
            evaluateMission(m.getId());
        }
    }

    private boolean evaluateByType(Mission mission, Map<String, Object> payload) {
        return switch (mission.getType()) {
            case RECAPTURE_RECENT_LOSS -> evaluateRecapture(mission, payload);
            case DIVERSITY_SPORT -> evaluateDiversity(mission, payload);
            case BREAK_ROUTE -> evaluateBreakRoute(mission, payload);
        };
    }

    /**
     * RECAPTURE: SUCCESS si l'équipe contrôle à nouveau le point payload.pointId
     */
    private boolean evaluateRecapture(Mission mission, Map<String, Object> payload) {
        Long pointId = toLong(payload.get("pointId"));
        if (pointId == null) return false;

        Optional<PointSportif> pointOpt = pointRepository.findById(pointId);
        if (pointOpt.isEmpty()) return false;

        PointSportif point = pointOpt.get();
        if (mission.getTeamId().equals(point.getControllingTeamId())) {
            mission.setProgressCurrent(1);
            return true;
        }
        return false;
    }

    /**
     * DIVERSITY: SUCCESS si une session du sport payload.sportId a été terminée
     * dans la zone/point après startsAt.
     */
    private boolean evaluateDiversity(Mission mission, Map<String, Object> payload) {
        Long sportId = toLong(payload.get("sportId"));
        Object pointIdObj = payload.get("pointId");
        if (sportId == null) return false;

        Instant startsAt = mission.getStartsAtInstant();
        List<Session> terminated = sessionRepository.findByState(SessionState.TERMINATED);

        for (Session s : terminated) {
            if (s.getSport() == null || s.getEndedAt() == null) continue;
            if (!sportId.equals(s.getSport().getId())) continue;

            // Vérifier le point si spécifié
            if (pointIdObj != null) {
                String expectedPointId = pointIdObj.toString();
                if (!expectedPointId.equals(s.getPointId())) continue;
            }

            Instant endedInstant = s.getEndedAt().atZone(java.time.ZoneId.systemDefault()).toInstant();
            if (endedInstant.isAfter(startsAt)) {
                mission.setProgressCurrent(1);
                return true;
            }
        }
        return false;
    }

    /**
     * BREAK_ROUTE: SUCCESS si l'équipe contrôle au moins 1 point appartenant
     * à la route payload.routeId (cassant le combo adverse).
     */
    private boolean evaluateBreakRoute(Mission mission, Map<String, Object> payload) {
        Long routeId = toLong(payload.get("routeId"));
        if (routeId == null) return false;

        Optional<Route> routeOpt = routeRepository.findById(routeId);
        if (routeOpt.isEmpty()) return false;

        Route route = routeOpt.get();
        boolean teamControlsAPoint = route.getPoints().stream()
                .anyMatch(p -> mission.getTeamId().equals(p.getControllingTeamId()));

        if (teamControlsAPoint) {
            mission.setProgressCurrent(1);
            return true;
        }
        return false;
    }

    /**
     * Marque la mission comme SUCCESS, attribue les récompenses à l'équipe,
     * et déclenche le hook de recompute route si applicable.
     */
    private void completeMissionSuccess(Mission mission) {
        mission.setStatus(MissionStatus.SUCCESS);
        mission.setCompletedAt(Instant.now().toString());

        // Attribuer les récompenses
        equipeRepository.findById(mission.getTeamId()).ifPresent(equipe -> {
            equipe.setPoints(equipe.getPoints() + mission.getRewardTeamPoints());
            equipe.setXp(equipe.getXp() + mission.getRewardTeamXp());
            equipeRepository.save(equipe);
        });

        // Hook: recompute route si mission BREAK_ROUTE
        if (mission.getType() == MissionType.BREAK_ROUTE) {
            Map<String, Object> payload = parsePayload(mission.getPayloadJson());
            Long routeId = toLong(payload.get("routeId"));
            if (routeId != null) {
                // TODO: appeler routeService.recompute(routeId) quand le service le supportera.
                // Pour l'instant, on log un message et on recalcule les bonus via TerritoryService.
                System.out.println(">>> HOOK: Route " + routeId + " combo cassé par équipe " + mission.getTeamId());
            }
        }
    }

    private Map<String, Object> parsePayload(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
