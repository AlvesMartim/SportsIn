package org.SportsIn.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryGameRepositoryTest {

    private InMemoryGameRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryGameRepository();
    }

    @Test
    void save_assignsId() {
        Game game = new Game();
        game.setPointId("a1");
        Game saved = repository.save(game);
        assertNotNull(saved.getId());
        assertTrue(saved.getId().startsWith("GAME_"));
    }

    @Test
    void save_preservesExistingId() {
        Game game = new Game();
        game.setId("CUSTOM_ID");
        Game saved = repository.save(game);
        assertEquals("CUSTOM_ID", saved.getId());
    }

    @Test
    void save_null_returnsNull() {
        assertNull(repository.save(null));
    }

    @Test
    void findById_found() {
        Game saved = repository.save(new Game());
        Optional<Game> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
    }

    @Test
    void findById_notFound() {
        assertTrue(repository.findById("NOPE").isEmpty());
    }

    @Test
    void findAll_empty() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void findAll_returnsAll() {
        repository.save(new Game());
        repository.save(new Game());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findByState_filters() {
        Game g1 = new Game();
        g1.setState(GameState.WAITING);
        repository.save(g1);

        Game g2 = new Game();
        g2.setState(GameState.COMPLETED);
        repository.save(g2);

        List<Game> waiting = repository.findByState(GameState.WAITING);
        assertEquals(1, waiting.size());
    }

    @Test
    void findByPointIdAndState_filters() {
        Game g1 = new Game();
        g1.setPointId("a1");
        g1.setState(GameState.WAITING);
        repository.save(g1);

        Game g2 = new Game();
        g2.setPointId("a2");
        g2.setState(GameState.WAITING);
        repository.save(g2);

        assertEquals(1, repository.findByPointIdAndState("a1", GameState.WAITING).size());
    }

    @Test
    void deleteById_existing() {
        Game saved = repository.save(new Game());
        assertTrue(repository.deleteById(saved.getId()));
        assertTrue(repository.findById(saved.getId()).isEmpty());
    }

    @Test
    void deleteById_nonExisting() {
        assertFalse(repository.deleteById("NOPE"));
    }

    @Test
    void existsById_found() {
        Game saved = repository.save(new Game());
        assertTrue(repository.existsById(saved.getId()));
    }

    @Test
    void existsById_notFound() {
        assertFalse(repository.existsById("NOPE"));
    }
}
