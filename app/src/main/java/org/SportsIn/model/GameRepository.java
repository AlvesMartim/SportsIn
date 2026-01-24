package org.SportsIn.model;

import java.util.List;
import java.util.Optional;

public interface GameRepository {
    Optional<Game> findById(String id);
    List<Game> findAll();
    List<Game> findByState(GameState state);
    List<Game> findByPointIdAndState(String pointId, GameState state);
    Game save(Game game);
    boolean deleteById(String id);
    boolean existsById(String id);
}
