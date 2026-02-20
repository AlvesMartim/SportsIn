package org.SportsIn.services;

import org.SportsIn.model.*;
import org.SportsIn.model.user.Equipe;
import org.SportsIn.repository.EquipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameServiceTest {

    private GameService gameService;
    private InMemoryGameRepository gameRepository;
    private InMemorySessionRepository sessionRepository;
    private EquipeRepository equipeRepository;

    private Equipe creator;
    private Equipe opponent;
    private Sport sport;

    @BeforeEach
    void setUp() {
        gameRepository = new InMemoryGameRepository();
        sessionRepository = new InMemorySessionRepository();
        equipeRepository = mock(EquipeRepository.class);
        gameService = new GameService(gameRepository, sessionRepository, equipeRepository);

        creator = new Equipe("Rouge");
        creator.setId(1L);
        opponent = new Equipe("Bleu");
        opponent.setId(2L);

        sport = new Sport();
        sport.setCode("FOOTBALL");
        sport.setName("Football");
    }

    // ---- create ----

    @Test
    void create_setsStateToWaiting() {
        Game game = new Game(null, sport, "arene1", creator);
        Game saved = gameService.create(game);

        assertNotNull(saved.getId());
        assertEquals(GameState.WAITING, saved.getState());
        assertNotNull(saved.getCreatedAt());
    }

    // ---- getAll / getById ----

    @Test
    void getAll_returnsAllGames() {
        gameService.create(new Game(null, sport, "a1", creator));
        gameService.create(new Game(null, sport, "a2", creator));
        assertEquals(2, gameService.getAll().size());
    }

    @Test
    void getById_existingGame() {
        Game saved = gameService.create(new Game(null, sport, "a1", creator));
        assertTrue(gameService.getById(saved.getId()).isPresent());
    }

    @Test
    void getById_nonExistingGame() {
        assertTrue(gameService.getById("NOPE").isEmpty());
    }

    // ---- getWaiting ----

    @Test
    void getWaiting_filtersCorrectly() {
        gameService.create(new Game(null, sport, "a1", creator));
        gameService.create(new Game(null, sport, "a2", creator));
        assertEquals(2, gameService.getWaiting().size());
    }

    // ---- getWaitingAtPoint ----

    @Test
    void getWaitingAtPoint_filtersCorrectly() {
        gameService.create(new Game(null, sport, "a1", creator));
        gameService.create(new Game(null, sport, "a2", creator));
        assertEquals(1, gameService.getWaitingAtPoint("a1").size());
    }

    // ---- joinGame ----

    @Test
    void joinGame_success() {
        when(equipeRepository.findById(2L)).thenReturn(Optional.of(opponent));
        Game saved = gameService.create(new Game(null, sport, "a1", creator));

        Optional<Game> joined = gameService.joinGame(saved.getId(), 2L);
        assertTrue(joined.isPresent());
        assertEquals(GameState.MATCHED, joined.get().getState());
        assertEquals("Bleu", joined.get().getOpponentTeam().getNom());
    }

    @Test
    void joinGame_sameTeamThrows() {
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(creator));
        Game saved = gameService.create(new Game(null, sport, "a1", creator));

        assertThrows(IllegalArgumentException.class,
                () -> gameService.joinGame(saved.getId(), 1L));
    }

    @Test
    void joinGame_alreadyMatchedThrows() {
        when(equipeRepository.findById(2L)).thenReturn(Optional.of(opponent));
        Game saved = gameService.create(new Game(null, sport, "a1", creator));
        gameService.joinGame(saved.getId(), 2L);

        assertThrows(IllegalStateException.class,
                () -> gameService.joinGame(saved.getId(), 2L));
    }

    @Test
    void joinGame_unknownTeamThrows() {
        when(equipeRepository.findById(99L)).thenReturn(Optional.empty());
        Game saved = gameService.create(new Game(null, sport, "a1", creator));

        assertThrows(IllegalArgumentException.class,
                () -> gameService.joinGame(saved.getId(), 99L));
    }

    @Test
    void joinGame_nonExistentGame() {
        Optional<Game> result = gameService.joinGame("NOPE", 2L);
        assertTrue(result.isEmpty());
    }

    // ---- startGame ----

    @Test
    void startGame_success() {
        when(equipeRepository.findById(2L)).thenReturn(Optional.of(opponent));
        Game saved = gameService.create(new Game(null, sport, "a1", creator));
        gameService.joinGame(saved.getId(), 2L);

        Optional<Game> started = gameService.startGame(saved.getId());
        assertTrue(started.isPresent());
        assertEquals(GameState.IN_PROGRESS, started.get().getState());
        assertNotNull(started.get().getSessionId());
        assertNotNull(started.get().getStartedAt());
    }

    @Test
    void startGame_notMatchedThrows() {
        Game saved = gameService.create(new Game(null, sport, "a1", creator));
        assertThrows(IllegalStateException.class,
                () -> gameService.startGame(saved.getId()));
    }

    @Test
    void startGame_createsSessionWithParticipants() {
        when(equipeRepository.findById(2L)).thenReturn(Optional.of(opponent));
        Game saved = gameService.create(new Game(null, sport, "a1", creator));
        gameService.joinGame(saved.getId(), 2L);
        gameService.startGame(saved.getId());

        Game started = gameService.getById(saved.getId()).get();
        Session session = sessionRepository.findById(started.getSessionId()).orElse(null);
        assertNotNull(session);
        assertEquals(SessionState.ACTIVE, session.getState());
        assertEquals(2, session.getParticipants().size());
    }

    // ---- completeGame ----

    @Test
    void completeGame_success() {
        when(equipeRepository.findById(2L)).thenReturn(Optional.of(opponent));
        Game saved = gameService.create(new Game(null, sport, "a1", creator));
        gameService.joinGame(saved.getId(), 2L);
        gameService.startGame(saved.getId());

        Optional<Game> completed = gameService.completeGame(saved.getId(), "1");
        assertTrue(completed.isPresent());
        assertEquals(GameState.COMPLETED, completed.get().getState());
        assertEquals("1", completed.get().getWinnerTeamId());
        assertNotNull(completed.get().getCompletedAt());
    }

    @Test
    void completeGame_notInProgressThrows() {
        Game saved = gameService.create(new Game(null, sport, "a1", creator));
        assertThrows(IllegalStateException.class,
                () -> gameService.completeGame(saved.getId(), "1"));
    }

    // ---- delete ----

    @Test
    void delete_existingGame() {
        Game saved = gameService.create(new Game(null, sport, "a1", creator));
        assertTrue(gameService.delete(saved.getId()));
        assertTrue(gameService.getById(saved.getId()).isEmpty());
    }

    @Test
    void delete_nonExistingGame() {
        assertFalse(gameService.delete("NOPE"));
    }
}
