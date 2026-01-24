package org.SportsIn.model;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class InMemorySessionRepository implements SessionRepository {

    private final Map<String, Session> database = new HashMap<>();

    @Override
    public Optional<Session> findById(String id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Session> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public List<Session> findByState(SessionState state) {
        return database.values().stream()
                .filter(session -> session.getState() == state)
                .collect(Collectors.toList());
    }

    @Override
    public Session save(Session session) {
        if (session == null) {
            return null;
        }
        if (session.getId() == null) {
            session.setId("SESSION_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        database.put(session.getId(), session);
        return session;
    }

    @Override
    public boolean deleteById(String id) {
        return database.remove(id) != null;
    }

    @Override
    public boolean existsById(String id) {
        return database.containsKey(id);
    }
}
