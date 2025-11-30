package org.SportsIn.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemorySessionRepository implements SessionRepository {

    private final Map<String, Session> database = new HashMap<>();

    @Override
    public Optional<Session> findById(String id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public void save(Session session) {
        if (session == null || session.getId() == null) {
            return;
        }
        database.put(session.getId(), session);
    }
}
