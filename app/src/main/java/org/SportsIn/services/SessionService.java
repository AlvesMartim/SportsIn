package org.SportsIn.services;

import org.SportsIn.model.EvaluationResult;
import org.SportsIn.model.Session;
import org.SportsIn.model.SessionRepository;
import org.SportsIn.model.SessionState;
import org.SportsIn.model.territory.PointSportif;
import org.SportsIn.model.territory.PointSportifRepository;

/**
 * Service agissant comme un chef d'orchestre pour gérer la logique métier
 * liée aux sessions de sport.
 */
public class SessionService {

    private final SessionRepository sessionRepository;
    private final PointSportifRepository pointSportifRepository;

    /**
     * Construit le service en lui fournissant ses dépendances (les repositories).
     * C'est ce qu'on appelle l'injection de dépendances.
     */
    public SessionService(SessionRepository sessionRepository, PointSportifRepository pointSportifRepository) {
        this.sessionRepository = sessionRepository;
        this.pointSportifRepository = pointSportifRepository;
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

        // Étape 2 : Identifier et mettre à jour le point sportif
        String pointIdStr = session.getPointId();
        if (pointIdStr != null) {
            try {
                Long pointId = Long.parseLong(pointIdStr);
                pointSportifRepository.findById(pointId).ifPresent(point -> {
                    System.out.println("Le point '" + point.getNom() + "' (ID: " + pointId + ") était contrôlé par l'équipe " + point.getControllingTeamId());
                    point.setControllingTeamId(winnerTeamId);
                    pointSportifRepository.save(point);
                    System.out.println("Il est maintenant contrôlé par l'équipe " + winnerTeamId);
                });
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
