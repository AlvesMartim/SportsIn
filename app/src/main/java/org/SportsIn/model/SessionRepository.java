package org.SportsIn.model;

import java.util.List;
import java.util.Optional;

public interface SessionRepository {
    Optional<Session> findById(String id);
    List<Session> findAll();
    List<Session> findByState(SessionState state);
    Session save(Session session);
    boolean deleteById(String id);
    boolean existsById(String id);
}
