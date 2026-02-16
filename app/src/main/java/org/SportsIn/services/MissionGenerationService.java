package org.SportsIn.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.SportsIn.model.Session;
import org.SportsIn.model.SessionRepository;
import org.SportsIn.model.SessionState;
import org.SportsIn.model.Sport;
import org.SportsIn.model.mission.*;
import org.SportsIn.model.territory.*;
import org.SportsIn.repository.MissionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Génère des missions dynamiques pour les équipes basées sur l'état du territoire,
 * l'historique des sessions, et les sports peu joués.
 */
@Service
public class MissionGenerationService {

    private static final int MAX_ACTIVE_MISSIONS_PER_TEAM = 3;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final MissionRepository missionRepository;
    private final PointSportifRepository pointRepository;
    private final ZoneRepository zoneRepository;
    private final RouteRepository routeRepository;
    private final SessionRepository sessionRepository;

    public MissionGenerationService(MissionRepository missionRepository,
                                    PointSportifRepository pointRepository,
                                    ZoneRepository zoneRepository,
                                    RouteRepository routeRepository,
                                    SessionRepository sessionRepository) {
        this.missionRepository = missionRepository;
        this.pointRepository = pointRepository;
        this.zoneRepository = zoneRepository;
        this.routeRepository = routeRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Génère des missions pour une équipe donnée.
     * Ne dépasse pas MAX_ACTIVE_MISSIONS_PER_TEAM missions actives.
     * Ne recrée pas une mission identique si déjà ACTIVE (même type + même payload clé).
     */
    public void generateForTeam(Long teamId) {
        long activeCount = missionRepository.countActiveByTeam(teamId);
        if (activeCount >= MAX_ACTIVE_MISSIONS_PER_TEAM) {
            return;
        }

        Set<String> existingKeys = missionRepository.findActiveByTeam(teamId).stream()
                .map(Mission::payloadKey)
                .collect(Collectors.toSet());

        int slotsAvailable = (int) (MAX_ACTIVE_MISSIONS_PER_TEAM - activeCount);
        List<Mission> candidates = new ArrayList<>();

        // R1: RECAPTURE_RECENT_LOSS
        tryGenerateRecaptureMission(teamId, existingKeys).ifPresent(candidates::add);

        // R2: DIVERSITY_SPORT
        tryGenerateDiversityMission(teamId, existingKeys).ifPresent(candidates::add);

        // R3: BREAK_ROUTE
        tryGenerateBreakRouteMission(teamId, existingKeys).ifPresent(candidates::add);

        // Sauvegarder dans la limite des slots
        int toSave = Math.min(slotsAvailable, candidates.size());
        for (int i = 0; i < toSave; i++) {
            missionRepository.save(candidates.get(i));
        }
    }

    /**
     * R1: Trouver un point non contrôlé par l'équipe.
     * Hypothèse: comme il n'y a pas d'historique de possession, on cherche un point
     * contrôlé par une autre équipe dans une zone où l'équipe a au moins un point.
     * Cela simule "un point perdu récemment" (l'équipe est présente dans la zone mais pas sur ce point).
     */
    private Optional<Mission> tryGenerateRecaptureMission(Long teamId, Set<String> existingKeys) {
        List<Zone> zones = zoneRepository.findAll();
        for (Zone zone : zones) {
            boolean teamHasPointInZone = zone.getPoints().stream()
                    .anyMatch(p -> teamId.equals(p.getControllingTeamId()));
            if (!teamHasPointInZone) continue;

            Optional<PointSportif> lostPoint = zone.getPoints().stream()
                    .filter(p -> p.getControllingTeamId() != null && !teamId.equals(p.getControllingTeamId()))
                    .findFirst();

            if (lostPoint.isPresent()) {
                PointSportif point = lostPoint.get();
                Map<String, Object> payload = Map.of(
                        "zoneId", zone.getId(),
                        "pointId", point.getId(),
                        "windowDays", 7
                );
                String payloadJson = toJson(payload);
                String key = MissionType.RECAPTURE_RECENT_LOSS.name() + ":" + payloadJson;
                if (existingKeys.contains(key)) return Optional.empty();

                Instant now = Instant.now();
                Mission m = new Mission();
                m.setTeamId(teamId);
                m.setType(MissionType.RECAPTURE_RECENT_LOSS);
                m.setStatus(MissionStatus.ACTIVE);
                m.setTitle("Reprendre le point " + point.getNom());
                m.setDescription("Reconquérir le point '" + point.getNom()
                        + "' dans la zone '" + zone.getNom() + "' avant expiration.");
                m.setPriority(MissionPriority.HIGH);
                m.setRewardTeamPoints(50);
                m.setRewardTeamXp(30);
                m.setTimestampsFromInstant(now, now, now.plus(3, ChronoUnit.DAYS));
                m.setPayloadJson(payloadJson);
                m.setProgressCurrent(0);
                m.setProgressTarget(1);
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    /**
     * R2: Trouver un sport peu joué dans une zone sur 14 jours.
     * Hypothèse: on compare les sports disponibles sur les points d'une zone
     * avec les sessions terminées récentes. Un sport sans session récente = candidat.
     */
    private Optional<Mission> tryGenerateDiversityMission(Long teamId, Set<String> existingKeys) {
        List<Zone> zones = zoneRepository.findAll();
        List<Session> terminatedSessions = sessionRepository.findByState(SessionState.TERMINATED);

        // Collecter les sports joués récemment (14 derniers jours) par point
        Set<String> recentSportPointCombos = new HashSet<>();
        Instant fourteenDaysAgo = Instant.now().minus(14, ChronoUnit.DAYS);
        for (Session s : terminatedSessions) {
            if (s.getEndedAt() != null && s.getSport() != null && s.getPointId() != null) {
                Instant endedInstant = s.getEndedAt().atZone(java.time.ZoneId.systemDefault()).toInstant();
                if (endedInstant.isAfter(fourteenDaysAgo)) {
                    recentSportPointCombos.add(s.getSport().getId() + ":" + s.getPointId());
                }
            }
        }

        for (Zone zone : zones) {
            for (PointSportif point : zone.getPoints()) {
                if (point.getSportsDisponibles() == null) continue;
                for (Sport sport : point.getSportsDisponibles()) {
                    String combo = sport.getId() + ":" + point.getId();
                    if (!recentSportPointCombos.contains(combo)) {
                        Map<String, Object> payload = Map.of(
                                "zoneId", zone.getId(),
                                "sportId", sport.getId(),
                                "sportCode", sport.getCode(),
                                "pointId", point.getId(),
                                "windowDays", 14
                        );
                        String payloadJson = toJson(payload);
                        String key = MissionType.DIVERSITY_SPORT.name() + ":" + payloadJson;
                        if (existingKeys.contains(key)) continue;

                        Instant now = Instant.now();
                        Mission m = new Mission();
                        m.setTeamId(teamId);
                        m.setType(MissionType.DIVERSITY_SPORT);
                        m.setStatus(MissionStatus.ACTIVE);
                        m.setTitle("Diversité: " + sport.getName() + " dans " + zone.getNom());
                        m.setDescription("Faire une session de " + sport.getName()
                                + " au point '" + point.getNom()
                                + "' (zone " + zone.getNom() + "). Aucune session de ce sport depuis 2 semaines.");
                        m.setPriority(MissionPriority.MEDIUM);
                        m.setRewardTeamPoints(30);
                        m.setRewardTeamXp(20);
                        m.setTimestampsFromInstant(now, now, now.plus(7, ChronoUnit.DAYS));
                        m.setPayloadJson(payloadJson);
                        m.setProgressCurrent(0);
                        m.setProgressTarget(1);
                        return Optional.of(m);
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * R3: Sélectionner une route contrôlée par une équipe adverse.
     * Une route est "contrôlée" si tous ses points appartiennent à la même équipe adversaire.
     * Mission: reprendre au moins 1 point de cette route.
     */
    private Optional<Mission> tryGenerateBreakRouteMission(Long teamId, Set<String> existingKeys) {
        List<Route> routes = routeRepository.findAll();

        for (Route route : routes) {
            if (route.getPoints() == null || route.getPoints().size() < 2) continue;

            // Vérifier si la route est entièrement contrôlée par un adversaire
            Long adversaryTeamId = null;
            boolean fullyControlled = true;
            for (PointSportif p : route.getPoints()) {
                Long owner = p.getControllingTeamId();
                if (owner == null || teamId.equals(owner)) {
                    fullyControlled = false;
                    break;
                }
                if (adversaryTeamId == null) {
                    adversaryTeamId = owner;
                } else if (!adversaryTeamId.equals(owner)) {
                    fullyControlled = false;
                    break;
                }
            }

            if (fullyControlled && adversaryTeamId != null) {
                Map<String, Object> payload = Map.of(
                        "routeId", route.getId(),
                        "adversaryTeamId", adversaryTeamId,
                        "minCount", 1
                );
                String payloadJson = toJson(payload);
                String key = MissionType.BREAK_ROUTE.name() + ":" + payloadJson;
                if (existingKeys.contains(key)) continue;

                Instant now = Instant.now();
                Mission m = new Mission();
                m.setTeamId(teamId);
                m.setType(MissionType.BREAK_ROUTE);
                m.setStatus(MissionStatus.ACTIVE);
                m.setTitle("Briser la route " + route.getNom());
                m.setDescription("Reprendre au moins 1 point de la route '"
                        + route.getNom() + "' contrôlée par l'équipe adverse.");
                m.setPriority(MissionPriority.HIGH);
                m.setRewardTeamPoints(75);
                m.setRewardTeamXp(50);
                m.setTimestampsFromInstant(now, now, now.plus(5, ChronoUnit.DAYS));
                m.setPayloadJson(payloadJson);
                m.setProgressCurrent(0);
                m.setProgressTarget(1);
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    private String toJson(Map<String, Object> map) {
        try {
            return MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }
}
