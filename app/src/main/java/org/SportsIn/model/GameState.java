package org.SportsIn.model;

/**
 * États possibles d'un jeu/match.
 */
public enum GameState {
    WAITING,      // En attente d'un adversaire
    MATCHED,      // Adversaire trouvé, prêt à démarrer
    IN_PROGRESS,  // Jeu en cours (session active)
    COMPLETED     // Jeu terminé
}
