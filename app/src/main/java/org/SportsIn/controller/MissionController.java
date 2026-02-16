package org.SportsIn.controller;

import org.SportsIn.dto.MissionDetailDTO;
import org.SportsIn.dto.MissionSummaryDTO;
import org.SportsIn.model.mission.Mission;
import org.SportsIn.model.mission.MissionStatus;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.repository.MissionRepository;
import org.SportsIn.services.MissionEvaluationService;
import org.SportsIn.services.MissionGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MissionController {

    private final MissionRepository missionRepository;
    private final EquipeRepository equipeRepository;
    private final MissionGenerationService generationService;
    private final MissionEvaluationService evaluationService;

    public MissionController(MissionRepository missionRepository,
                             EquipeRepository equipeRepository,
                             MissionGenerationService generationService,
                             MissionEvaluationService evaluationService) {
        this.missionRepository = missionRepository;
        this.equipeRepository = equipeRepository;
        this.generationService = generationService;
        this.evaluationService = evaluationService;
    }

    /**
     * GET /api/teams/{teamId}/missions?status=ACTIVE|SUCCESS|FAILED|EXPIRED
     * Liste les missions d'une équipe, triées par endsAt asc (ACTIVE) ou completedAt desc (autres).
     */
    @GetMapping("/teams/{teamId}/missions")
    public ResponseEntity<?> getMissions(@PathVariable Long teamId,
                                         @RequestParam(required = false) String status) {
        if (!equipeRepository.existsById(teamId)) {
            return ResponseEntity.notFound().build();
        }

        List<Mission> missions;
        if (status != null && !status.isBlank()) {
            MissionStatus missionStatus;
            try {
                missionStatus = MissionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Statut invalide: " + status
                        + ". Valeurs acceptées: ACTIVE, SUCCESS, FAILED, EXPIRED");
            }
            missions = missionRepository.findByTeamIdAndStatus(teamId, missionStatus);
        } else {
            missions = missionRepository.findByTeamIdOrderByEndsAtAsc(teamId);
        }

        // Tri: ACTIVE par endsAt asc, autres par completedAt desc
        if (status != null && "ACTIVE".equalsIgnoreCase(status)) {
            missions.sort(Comparator.comparing(Mission::getEndsAt, Comparator.nullsLast(Comparator.naturalOrder())));
        } else if (status != null) {
            missions.sort(Comparator.comparing(
                    Mission::getCompletedAt,
                    Comparator.nullsLast(Comparator.reverseOrder())
            ));
        }

        List<MissionSummaryDTO> dtos = missions.stream()
                .map(MissionSummaryDTO::from)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/missions/{missionId}
     * Détail d'une mission.
     */
    @GetMapping("/missions/{missionId}")
    public ResponseEntity<?> getMission(@PathVariable Long missionId) {
        return missionRepository.findById(missionId)
                .map(m -> ResponseEntity.ok(MissionDetailDTO.from(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/teams/{teamId}/missions/generate
     * Force la génération de missions (debug). Renvoie les missions actives.
     */
    @PostMapping("/teams/{teamId}/missions/generate")
    public ResponseEntity<?> generateMissions(@PathVariable Long teamId) {
        if (!equipeRepository.existsById(teamId)) {
            return ResponseEntity.notFound().build();
        }

        generationService.generateForTeam(teamId);

        List<MissionSummaryDTO> activeMissions = missionRepository.findActiveByTeam(teamId).stream()
                .map(MissionSummaryDTO::from)
                .toList();
        return ResponseEntity.ok(activeMissions);
    }

    /**
     * POST /api/missions/{missionId}/refresh
     * Force l'évaluation d'une mission (debug). Renvoie le détail mis à jour.
     */
    @PostMapping("/missions/{missionId}/refresh")
    public ResponseEntity<?> refreshMission(@PathVariable Long missionId) {
        if (!missionRepository.existsById(missionId)) {
            return ResponseEntity.notFound().build();
        }

        Mission evaluated = evaluationService.evaluateMission(missionId);
        return ResponseEntity.ok(MissionDetailDTO.from(evaluated));
    }
}
