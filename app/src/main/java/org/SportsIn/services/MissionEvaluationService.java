package org.SportsIn.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.SportsIn.model.Arene;
import org.SportsIn.model.Session;
import org.SportsIn.model.SessionRepository;
import org.SportsIn.model.SessionState;
import org.SportsIn.model.mission.*;
import org.SportsIn.repository.AreneRepository;
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
    private final AreneRepository areneRepository;
    private final SessionRepository sessionRepository;

    public MissionEvaluationService(MissionRepository missionRepository,
                                    EquipeRepository equipeRepository,
                                    AreneRepository areneRepository,
                                    SessionRepository sessionRepository) {
        this.missionRepository = missionRepository;
        this.equipeRepository = equipeRepository;
        this.areneRepository = areneRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public Mission evaluateMission(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission introuvable: " + missionId));

        if (mission.getStatus() != MissionStatus.ACTIVE) {
            return mission;
        }

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

    @Transactional
    public void evaluateActiveMissionsForTeam(Long teamId) {
        List<Mission> activeMissions = missionRepository.findActiveByTeam(teamId);
        for (Mission m : activeMissions) {
            evaluateMission(m.getId());
        }
    }

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
     * RECAPTURE: SUCCESS si l'équipe contrôle l'arène (arenaId).
     */
    private boolean evaluateRecapture(Mission mission, Map<String, Object> payload) {
        Object arenaIdObj = payload.get("arenaId");
        if (arenaIdObj == null) return false;

        String arenaId = arenaIdObj.toString();
        Optional<Arene> areneOpt = areneRepository.findById(arenaId);
        if (areneOpt.isEmpty()) return false;

        Arene arene = areneOpt.get();
        if (arene.getControllingTeam() != null
                && mission.getTeamId().equals(arene.getControllingTeam().getId())) {
            mission.setProgressCurrent(1);
            return true;
        }
        return false;
    }

    /**
     * DIVERSITY: SUCCESS si une session du sport (sportCode) a été terminée
     * sur l'arène (arenaId) après startsAt.
     */
    private boolean evaluateDiversity(Mission mission, Map<String, Object> payload) {
        Object arenaIdObj = payload.get("arenaId");
        Object sportCodeObj = payload.get("sportCode");
        if (sportCodeObj == null) return false;
        String sportCode = sportCodeObj.toString();

        Instant startsAt = mission.getStartsAtInstant();
        List<Session> terminated = sessionRepository.findByState(SessionState.TERMINATED);

        for (Session s : terminated) {
            if (s.getSport() == null || s.getEndedAt() == null) continue;
            if (!sportCode.equals(s.getSport().getCode())) continue;

            if (arenaIdObj != null) {
                String expectedArenaId = arenaIdObj.toString();
                if (!expectedArenaId.equals(s.getPointId())) continue;
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
     * BREAK_ROUTE: SUCCESS si l'équipe contrôle l'arène (arenaId).
     */
    private boolean evaluateBreakRoute(Mission mission, Map<String, Object> payload) {
        Object arenaIdObj = payload.get("arenaId");
        if (arenaIdObj == null) return false;

        String arenaId = arenaIdObj.toString();
        Optional<Arene> areneOpt = areneRepository.findById(arenaId);
        if (areneOpt.isEmpty()) return false;

        Arene arene = areneOpt.get();
        if (arene.getControllingTeam() != null
                && mission.getTeamId().equals(arene.getControllingTeam().getId())) {
            mission.setProgressCurrent(1);
            return true;
        }
        return false;
    }

    private void completeMissionSuccess(Mission mission) {
        mission.setStatus(MissionStatus.SUCCESS);
        mission.setCompletedAt(Instant.now().toString());

        equipeRepository.findById(mission.getTeamId()).ifPresent(equipe -> {
            equipe.setPoints(equipe.getPoints() + mission.getRewardTeamPoints());
            equipe.setXp(equipe.getXp() + mission.getRewardTeamXp());
            equipeRepository.save(equipe);
        });
    }

    private Map<String, Object> parsePayload(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
