package org.SportsIn.services;

import org.SportsIn.model.EvaluationResult;
import org.SportsIn.model.Session;
import org.SportsIn.model.SessionRepository;
import org.SportsIn.model.SessionState;
import org.springframework.stereotype.Service;

/**
 * Service agissant comme un chef d'orchestre pour gérer la logique métier
 * liée aux sessions de sport.
 */
@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final TerritoryService territoryService;
    private final XpGrantService xpGrantService;
    private final RuleEvaluationService ruleEvaluationService;

    public SessionService(SessionRepository sessionRepository,
                          TerritoryService territoryService,
                          XpGrantService xpGrantService,
                          RuleEvaluationService ruleEvaluationService) {
        this.sessionRepository = sessionRepository;
        this.territoryService = territoryService;
        this.xpGrantService = xpGrantService;
        this.ruleEvaluationService = ruleEvaluationService;
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
        if (pointId != null) {
            // On vérifie si l'équipe gagnante bénéficie d'un bonus sur cette arène
            double bonusMultiplier = territoryService.getScoreBonusForTeamOnPoint(winnerTeamId, pointId);
            
            if (bonusMultiplier > 0) {
                System.out.println(">>> BONUS APPLIQUÉ ! L'équipe " + winnerTeamId + " bénéficie d'un boost de " + (bonusMultiplier * 100) + "% grâce à ses routes.");
            }

            // Déléguer la gestion du territoire au TerritoryService
            territoryService.updateTerritoryControl(pointId, winnerTeamId);
        }

        // Étape 3 : Mettre à jour l'état final de la session
        session.setWinnerParticipantId(winnerTeamId.toString());
        session.setState(SessionState.TERMINATED);
        sessionRepository.save(session);

        System.out.println("La session " + sessionId + " est terminée. Vainqueur: équipe " + winnerTeamId);
    }
}
