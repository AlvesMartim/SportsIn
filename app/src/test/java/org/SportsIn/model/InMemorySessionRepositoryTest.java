package org.SportsIn.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemorySessionRepositoryTest {

    private InMemorySessionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySessionRepository();
    }

    @Test
    void save_assignsId() {
        Session session = new Session();
        Session saved = repository.save(session);
        assertNotNull(saved.getId());
        assertTrue(saved.getId().startsWith("SESSION_"));
    }

    @Test
    void save_preservesExistingId() {
        Session session = new Session();
        session.setId("MY_SESSION");
        Session saved = repository.save(session);
        assertEquals("MY_SESSION", saved.getId());
    }

    @Test
    void save_null_returnsNull() {
        assertNull(repository.save(null));
    }

    @Test
    void findById_found() {
        Session saved = repository.save(new Session());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_notFound() {
        assertTrue(repository.findById("NOPE").isEmpty());
    }

    @Test
    void findAll_returnsAll() {
        repository.save(new Session());
        repository.save(new Session());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findByState_filters() {
        Session active = new Session();
        active.setState(SessionState.ACTIVE);
        repository.save(active);

        Session terminated = new Session();
        terminated.setState(SessionState.TERMINATED);
        repository.save(terminated);

        List<Session> activeList = repository.findByState(SessionState.ACTIVE);
        assertEquals(1, activeList.size());
    }

    @Test
    void deleteById_existing() {
        Session saved = repository.save(new Session());
        assertTrue(repository.deleteById(saved.getId()));
        assertTrue(repository.findById(saved.getId()).isEmpty());
    }

    @Test
    void deleteById_nonExisting() {
        assertFalse(repository.deleteById("NOPE"));
    }

    @Test
    void existsById_found() {
        Session saved = repository.save(new Session());
        assertTrue(repository.existsById(saved.getId()));
    }

    @Test
    void existsById_notFound() {
        assertFalse(repository.existsById("NOPE"));
    }
}
