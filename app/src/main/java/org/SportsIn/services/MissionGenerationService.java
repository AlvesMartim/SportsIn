package org.SportsIn.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.SportsIn.model.Arene;
import org.SportsIn.model.Session;
import org.SportsIn.model.SessionRepository;
import org.SportsIn.model.SessionState;
import org.SportsIn.model.mission.*;
import org.SportsIn.repository.AreneRepository;
import org.SportsIn.repository.MissionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Génère des missions dynamiques pour les équipes basées sur l'état des arènes,
 * l'historique des sessions, et les sports peu joués.
 */
@Service
public class MissionGenerationService {

    private static final int MAX_ACTIVE_MISSIONS_PER_TEAM = 3;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final MissionRepository missionRepository;
    private final AreneRepository areneRepository;
    private final SessionRepository sessionRepository;

    public MissionGenerationService(MissionRepository missionRepository,
                                    AreneRepository areneRepository,
                                    SessionRepository sessionRepository) {
        this.missionRepository = missionRepository;
        this.areneRepository = areneRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Génère des missions pour une équipe donnée.
     * Ne dépasse pas MAX_ACTIVE_MISSIONS_PER_TEAM missions actives.
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

        // R1: RECAPTURE_RECENT_LOSS — reprendre une arène contrôlée par un adversaire
        tryGenerateRecaptureMission(teamId, existingKeys).ifPresent(candidates::add);

        // R2: DIVERSITY_SPORT — jouer un sport peu joué sur une arène
        tryGenerateDiversityMission(teamId, existingKeys).ifPresent(candidates::add);

        // R3: BREAK_ROUTE — briser le contrôle d'une 2e arène adverse
        tryGenerateBreakRouteMission(teamId, existingKeys).ifPresent(candidates::add);

        int toSave = Math.min(slotsAvailable, candidates.size());
        for (int i = 0; i < toSave; i++) {
            missionRepository.save(candidates.get(i));
        }
    }

    /**
     * R1: Trouver une arène contrôlée par un adversaire.
     */
    private Optional<Mission> tryGenerateRecaptureMission(Long teamId, Set<String> existingKeys) {
        List<Arene> allArenes = areneRepository.findAll();

        for (Arene arene : allArenes) {
            if (arene.getControllingTeam() == null) continue;
            Long ownerId = arene.getControllingTeam().getId();
            if (teamId.equals(ownerId)) continue;

            Map<String, Object> payload = Map.of(
                    "arenaId", arene.getId(),
                    "arenaName", arene.getNom(),
                    "windowDays", 7
            );
            String payloadJson = toJson(payload);
            String key = MissionType.RECAPTURE_RECENT_LOSS.name() + ":" + payloadJson;
            if (existingKeys.contains(key)) continue;

            Instant now = Instant.now();
            Mission m = new Mission();
            m.setTeamId(teamId);
            m.setType(MissionType.RECAPTURE_RECENT_LOSS);
            m.setStatus(MissionStatus.ACTIVE);
            m.setTitle("Reprendre " + arene.getNom());
            m.setDescription("Reconquérir l'arène '" + arene.getNom()
                    + "' actuellement contrôlée par une équipe adverse. Gagnez un match sur cette arène !");
            m.setPriority(MissionPriority.HIGH);
            m.setRewardTeamPoints(50);
            m.setRewardTeamXp(30);
            m.setTimestampsFromInstant(now, now, now.plus(3, ChronoUnit.DAYS));
            m.setPayloadJson(payloadJson);
            m.setProgressCurrent(0);
            m.setProgressTarget(1);
            return Optional.of(m);
        }
        return Optional.empty();
    }

    /**
     * R2: Trouver un sport peu joué sur une arène.
     */
    private Optional<Mission> tryGenerateDiversityMission(Long teamId, Set<String> existingKeys) {
        List<Arene> allArenes = areneRepository.findAll();
        List<Session> terminatedSessions = sessionRepository.findByState(SessionState.TERMINATED);

        Set<String> recentCombos = new HashSet<>();
        Instant fourteenDaysAgo = Instant.now().minus(14, ChronoUnit.DAYS);
        for (Session s : terminatedSessions) {
            if (s.getEndedAt() != null && s.getSport() != null && s.getPointId() != null) {
                Instant endedInstant = s.getEndedAt().atZone(java.time.ZoneId.systemDefault()).toInstant();
                if (endedInstant.isAfter(fourteenDaysAgo)) {
                    recentCombos.add(s.getSport().getCode() + ":" + s.getPointId());
                }
            }
        }

        for (Arene arene : allArenes) {
            if (arene.getSportsDisponibles() == null) continue;
            for (String sportCode : arene.getSportsDisponibles()) {
                String combo = sportCode + ":" + arene.getId();
                if (recentCombos.contains(combo)) continue;

                Map<String, Object> payload = Map.of(
                        "arenaId", arene.getId(),
                        "arenaName", arene.getNom(),
                        "sportCode", sportCode,
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
                m.setTitle("Diversité: " + sportCode + " à " + arene.getNom());
                m.setDescription("Jouer une session de " + sportCode
                        + " à l'arène '" + arene.getNom()
                        + "'. Aucune session de ce sport ici depuis 2 semaines !");
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
        return Optional.empty();
    }

    /**
     * R3: Briser le contrôle d'une 2e arène adverse.
     * Sélectionne une arène différente de celle déjà en R1.
     */
    private Optional<Mission> tryGenerateBreakRouteMission(Long teamId, Set<String> existingKeys) {
        List<Arene> allArenes = areneRepository.findAll();
        List<Arene> adversaryArenas = allArenes.stream()
                .filter(a -> a.getControllingTeam() != null && !teamId.equals(a.getControllingTeam().getId()))
                .toList();

        // Prendre la 2e arène adverse (la 1ère est déjà en R1)
        if (adversaryArenas.size() >= 2) {
            Arene arene = adversaryArenas.get(1);
            Map<String, Object> payload = Map.of(
                    "arenaId", arene.getId(),
                    "arenaName", arene.getNom(),
                    "adversaryTeamId", arene.getControllingTeam().getId(),
                    "minCount", 1
            );
            String payloadJson = toJson(payload);
            String key = MissionType.BREAK_ROUTE.name() + ":" + payloadJson;
            if (existingKeys.contains(key)) return Optional.empty();

            Instant now = Instant.now();
            Mission m = new Mission();
            m.setTeamId(teamId);
            m.setType(MissionType.BREAK_ROUTE);
            m.setStatus(MissionStatus.ACTIVE);
            m.setTitle("Briser le contrôle de " + arene.getNom());
            m.setDescription("Reprendre l'arène '" + arene.getNom()
                    + "' contrôlée par l'équipe adverse. Cassez leur domination !");
            m.setPriority(MissionPriority.HIGH);
            m.setRewardTeamPoints(75);
            m.setRewardTeamXp(50);
            m.setTimestampsFromInstant(now, now, now.plus(5, ChronoUnit.DAYS));
            m.setPayloadJson(payloadJson);
            m.setProgressCurrent(0);
            m.setProgressTarget(1);
            return Optional.of(m);
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
