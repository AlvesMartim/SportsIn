package org.SportsIn.services;

import org.SportsIn.model.EvaluationResult;
import org.SportsIn.model.Session;
import org.SportsIn.model.SessionRepository;
import org.SportsIn.model.SessionResult;
import org.SportsIn.model.SessionState;
import org.SportsIn.weather.SessionWeatherImpact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service agissant comme un chef d'orchestre pour gérer la logique métier
 * liée aux sessions de sport.
 */
@Service
public class SessionService {

    private static final double BASE_INFLUENCE_GAIN = 25.0;

    private final SessionRepository sessionRepository;
    private final TerritoryService territoryService;
    private final XpGrantService xpGrantService;
    private final RuleEvaluationService ruleEvaluationService;
    private final org.SportsIn.weather.SessionWeatherService sessionWeatherService;
    private final WeatherAffinityService weatherAffinityService;

    @Autowired
    public SessionService(SessionRepository sessionRepository,
                          TerritoryService territoryService,
                          XpGrantService xpGrantService,
                          RuleEvaluationService ruleEvaluationService,
                          org.SportsIn.weather.SessionWeatherService sessionWeatherService,
                          WeatherAffinityService weatherAffinityService) {
        this.sessionRepository = sessionRepository;
        this.territoryService = territoryService;
        this.xpGrantService = xpGrantService;
        this.ruleEvaluationService = ruleEvaluationService;
        this.sessionWeatherService = sessionWeatherService;
        this.weatherAffinityService = weatherAffinityService;
    }

    public SessionService(SessionRepository sessionRepository,
                          TerritoryService territoryService,
                          XpGrantService xpGrantService,
                          RuleEvaluationService ruleEvaluationService) {
        this(sessionRepository, territoryService, xpGrantService, ruleEvaluationService, null, null);
    }

    /**
     * Finalise une session : détermine le vainqueur, met à jour le contrôle de l'arène
     * et sauvegarde l'état final de la session.
     *
     * @param sessionId L'ID de la session à traiter.
     */
    public void processSessionCompletion(String sessionId) {
        // Étape 0 : Récupérer la session
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session non trouvée avec l'ID: " + sessionId));

        if (session.getEndedAt() == null) {
            session.setEndedAt(LocalDateTime.now());
        }

        // Étape 1 : Déterminer le vainqueur via le service de règles
        EvaluationResult verdict = ruleEvaluationService.evaluateVictory(session);
        if (verdict == null || verdict.getWinnerParticipantId() == null) {
            System.out.println("La session " + sessionId + " s'est terminée sans vainqueur. Pas de changement de territoire.");
            session.setState(SessionState.TERMINATED);
            sessionRepository.save(session);
            return;
        }
        
        Long winnerTeamId;
        try {
            winnerTeamId = Long.parseLong(verdict.getWinnerParticipantId());
        } catch (NumberFormatException e) {
            System.err.println("Erreur: L'ID du gagnant n'est pas un nombre valide: " + verdict.getWinnerParticipantId());
            return;
        }

        // --- Attribution d'XP de match ---
        xpGrantService.grantMatchXp(winnerTeamId, true);
        // Attribuer XP de participation aux perdants
        for (var participant : session.getParticipants()) {
            try {
                Long participantId = Long.parseLong(participant.getId());
                if (!participantId.equals(winnerTeamId)) {
                    xpGrantService.grantMatchXp(participantId, false);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // --- Application du Bonus d'influence ---
        String pointId = session.getPointId();
        SessionWeatherImpact weatherImpact = sessionWeatherService != null
                ? sessionWeatherService.analyze(session)
                : SessionWeatherImpact.neutral("WEATHER_DISABLED_IN_TEST");

        double weatherBonus = weatherImpact.weatherInfluenceBonus();
        double affinityBonus = weatherAffinityService != null
                ? weatherAffinityService.computeAffinityBonus(winnerTeamId, weatherImpact)
                : 0.0;

        if (pointId != null) {
            // On vérifie si l'équipe gagnante bénéficie d'un bonus sur cette arène
            double bonusMultiplier = territoryService.getScoreBonusForTeamOnPoint(winnerTeamId, pointId);

            double totalModifier = bonusMultiplier + weatherBonus + affinityBonus;
            double influenceGain = BASE_INFLUENCE_GAIN * Math.max(0.25, 1.0 + totalModifier);

            applyWeatherMetadata(session, weatherImpact, weatherBonus, affinityBonus, totalModifier);

            if (totalModifier > 0) {
                System.out.println(">>> BONUS APPLIQUE ! equipe " + winnerTeamId
                        + " | route/perks=" + String.format("%.2f", bonusMultiplier)
                        + " | meteo=" + String.format("%.2f", weatherBonus)
                        + " | affinite=" + String.format("%.2f", affinityBonus)
                        + " | total=" + String.format("%.2f", totalModifier));
            }

            // Déléguer la gestion du territoire au TerritoryService
            territoryService.updateTerritoryControl(pointId, winnerTeamId, influenceGain);
        } else {
            applyWeatherMetadata(session, weatherImpact, weatherBonus, affinityBonus, weatherBonus + affinityBonus);
        }

        // Étape 3 : Mettre à jour l'état final de la session
        session.setWinnerParticipantId(winnerTeamId.toString());
        session.setState(SessionState.TERMINATED);
        sessionRepository.save(session);

        System.out.println("La session " + sessionId + " est terminée. Vainqueur: équipe " + winnerTeamId);
    }

    private void applyWeatherMetadata(Session session,
                                      SessionWeatherImpact weatherImpact,
                                      double weatherBonus,
                                      double affinityBonus,
                                      double totalModifier) {
        SessionResult result = session.getResult();
        if (result == null) {
            result = new SessionResult(session, null);
            session.setResult(result);
        }

        result.setWeatherHardshipIndex(weatherImpact.hardshipIndex());
        result.setWeatherInfluenceBonus(weatherBonus);
        result.setWeatherAffinityBonus(affinityBonus);
        result.setTotalInfluenceModifier(totalModifier);
        result.setWeatherSource(weatherImpact.source());
        result.setWeatherTags(weatherImpact.tagsAsCsv());

        if (weatherImpact.snapshot() != null && !weatherImpact.snapshot().isUnknown()) {
            result.setWeatherTemperatureC(weatherImpact.snapshot().temperatureC());
            result.setWeatherWindSpeedMps(weatherImpact.snapshot().windSpeedMps());
            result.setWeatherPrecipitationMm(weatherImpact.snapshot().precipitationMm());
            result.setWeatherSummary(weatherImpact.snapshot().weatherMain() + " - " + weatherImpact.snapshot().description());
        } else {
            result.setWeatherSummary("Donnees meteo indisponibles");
        }
    }
}
