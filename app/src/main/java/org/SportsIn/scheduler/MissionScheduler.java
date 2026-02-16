package org.SportsIn.scheduler;

import org.SportsIn.model.user.Equipe;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.services.MissionEvaluationService;
import org.SportsIn.services.MissionGenerationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduler pour la gestion automatique des missions.
 * - Toutes les 10 minutes: expire et évalue les missions actives.
 * - Tous les jours à 06:00 Europe/Paris: génère des missions pour toutes les équipes.
 *
 * Peut être désactivé via la propriété mission.scheduler.enabled=false.
 */
@Component
@ConditionalOnProperty(name = "mission.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class MissionScheduler {

    private final MissionEvaluationService evaluationService;
    private final MissionGenerationService generationService;
    private final EquipeRepository equipeRepository;

    public MissionScheduler(MissionEvaluationService evaluationService,
                            MissionGenerationService generationService,
                            EquipeRepository equipeRepository) {
        this.evaluationService = evaluationService;
        this.generationService = generationService;
        this.equipeRepository = equipeRepository;
    }

    /**
     * Toutes les 10 minutes: expire les missions dépassées puis évalue les actives.
     */
    @Scheduled(fixedRate = 600_000)
    public void expireAndEvaluate() {
        evaluationService.expireActiveMissions();
        evaluationService.evaluateAllActiveMissions();
    }

    /**
     * Tous les jours à 06:00 Europe/Paris: génère des missions pour toutes les équipes.
     */
    @Scheduled(cron = "0 0 6 * * *", zone = "Europe/Paris")
    public void generateMissionsForAllTeams() {
        List<Equipe> teams = equipeRepository.findAll();
        for (Equipe team : teams) {
            generationService.generateForTeam(team.getId());
        }
    }
}
