package org.SportsIn.model;

import java.util.Optional;

public interface SessionRepository {
    Optional<Session> findById(String id);
    void save(Session session);
}
