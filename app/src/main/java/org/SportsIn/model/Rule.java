package org.SportsIn.model;

/**
 * Interface pour toutes les règles de jeu (victoire, score, etc.).
 * Chaque règle doit pouvoir être évaluée à partir d'une session.
 */
public interface Rule {
    /**
     * Évalue la règle sur la base des données d'une session.
     * @param session La session de jeu complète à évaluer.
     * @return Un objet EvaluationResult contenant le verdict.
     */
    EvaluationResult evaluate(Session session);
}
