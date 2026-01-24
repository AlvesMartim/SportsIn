package org.SportsIn.services;

import org.SportsIn.model.*;
import org.SportsIn.model.user.Equipe;
import org.SportsIn.repository.EquipeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les jeux/matchs et le matchmaking.
 */
@Service
public class GameService {

    private final GameRepository gameRepository;
    private final SessionRepository sessionRepository;
    private final EquipeRepository equipeRepository;

    public GameService(GameRepository gameRepository,
                       SessionRepository sessionRepository,
                       EquipeRepository equipeRepository) {
        this.gameRepository = gameRepository;
        this.sessionRepository = sessionRepository;
        this.equipeRepository = equipeRepository;
    }

    public List<Game> getAll() {
        return gameRepository.findAll();
    }

    public Optional<Game> getById(String id) {
        return gameRepository.findById(id);
    }

    public List<Game> getWaiting() {
        return gameRepository.findByState(GameState.WAITING);
    }

    public List<Game> getWaitingAtPoint(String pointId) {
        return gameRepository.findByPointIdAndState(pointId, GameState.WAITING);
    }

    /**
     * Crée un nouveau jeu. L'équipe créatrice attend un adversaire.
     */
    public Game create(Game game) {
        game.setState(GameState.WAITING);
        game.setCreatedAt(LocalDateTime.now());
        return gameRepository.save(game);
    }

    /**
     * Une équipe rejoint un jeu existant comme adversaire.
     * Le jeu passe à l'état MATCHED.
     */
    public Optional<Game> joinGame(String gameId, Long opponentTeamId) {
        return gameRepository.findById(gameId).map(game -> {
            if (game.getState() != GameState.WAITING) {
                throw new IllegalStateException("Ce jeu n'est plus en attente d'adversaire");
            }

            // Vérifier que l'équipe adverse existe
            Equipe opponent = equipeRepository.findById(opponentTeamId)
                    .orElseThrow(() -> new IllegalArgumentException("Équipe adverse non trouvée"));

            // Vérifier que ce n'est pas la même équipe
            if (game.getCreatorTeam() != null &&
                game.getCreatorTeam().getId().equals(opponentTeamId)) {
                throw new IllegalArgumentException("Une équipe ne peut pas s'affronter elle-même");
            }

            game.setOpponentTeam(opponent);
            game.setState(GameState.MATCHED);
            return gameRepository.save(game);
        });
    }

    /**
     * Démarre le jeu : crée une session et passe le jeu à IN_PROGRESS.
     */
    public Optional<Game> startGame(String gameId) {
        return gameRepository.findById(gameId).map(game -> {
            if (game.getState() != GameState.MATCHED) {
                throw new IllegalStateException("Le jeu doit être en état MATCHED pour démarrer");
            }

            // Créer une session pour ce jeu
            Session session = new Session();
            session.setSport(game.getSport());
            session.setPointId(game.getPointId());
            session.setState(SessionState.ACTIVE);
            session.setCreatedAt(LocalDateTime.now());

            // Créer les participants
            List<Participant> participants = new ArrayList<>();
            if (game.getCreatorTeam() != null) {
                participants.add(new Participant(
                    game.getCreatorTeam().getId().toString(),
                    game.getCreatorTeam().getNom(),
                    ParticipantType.TEAM
                ));
            }
            if (game.getOpponentTeam() != null) {
                participants.add(new Participant(
                    game.getOpponentTeam().getId().toString(),
                    game.getOpponentTeam().getNom(),
                    ParticipantType.TEAM
                ));
            }
            session.setParticipants(participants);

            Session savedSession = sessionRepository.save(session);

            game.setSessionId(savedSession.getId());
            game.setState(GameState.IN_PROGRESS);
            game.setStartedAt(LocalDateTime.now());

            return gameRepository.save(game);
        });
    }

    /**
     * Termine le jeu avec les résultats.
     */
    public Optional<Game> completeGame(String gameId, String winnerTeamId) {
        return gameRepository.findById(gameId).map(game -> {
            if (game.getState() != GameState.IN_PROGRESS) {
                throw new IllegalStateException("Le jeu doit être en cours pour être terminé");
            }

            game.setWinnerTeamId(winnerTeamId);
            game.setState(GameState.COMPLETED);
            game.setCompletedAt(LocalDateTime.now());

            return gameRepository.save(game);
        });
    }

    public boolean delete(String id) {
        if (gameRepository.existsById(id)) {
            return gameRepository.deleteById(id);
        }
        return false;
    }
}
