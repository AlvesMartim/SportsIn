package org.SportsIn.services;

import org.SportsIn.model.EvaluationResult;
import org.SportsIn.model.Session;
import org.SportsIn.model.SessionRepository;
import org.SportsIn.model.SessionState;

/**
 * Service agissant comme un chef d'orchestre pour gérer la logique métier
 * liée aux sessions de sport.
 */
public class SessionService {

    private final SessionRepository sessionRepository;
    private final TerritoryService territoryService;

    /**
     * Construit le service en lui fournissant ses dépendances.
     */
    public SessionService(SessionRepository sessionRepository, TerritoryService territoryService) {
        this.sessionRepository = sessionRepository;
        this.territoryService = territoryService;
    }

    /**
     * Finalise une session : détermine le vainqueur, met à jour le contrôle du point
     * et sauvegarde l'état final de la session.
     *
     * @param sessionId L'ID de la session à traiter.
     */
    public void processSessionCompletion(String sessionId) {
        // Étape 0 : Récupérer la session
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session non trouvée avec l'ID: " + sessionId));

        // Étape 1 : Déterminer le vainqueur
        EvaluationResult verdict = session.getResult().evaluateRules();
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

        // Étape 2 : Déléguer la gestion du territoire au TerritoryService
        String pointIdStr = session.getPointId();
        if (pointIdStr != null) {
            try {
                Long pointId = Long.parseLong(pointIdStr);
                territoryService.updateTerritoryControl(pointId, winnerTeamId);
            } catch (NumberFormatException e) {
                System.err.println("Erreur: L'ID du point n'est pas un nombre valide: " + pointIdStr);
            }
        }

        // Étape 3 : Mettre à jour l'état final de la session
        session.setWinnerParticipantId(winnerTeamId.toString());
        session.setState(SessionState.TERMINATED);
        sessionRepository.save(session);

        System.out.println("La session " + sessionId + " est terminée. Vainqueur: équipe " + winnerTeamId);
    }
}
