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
public class InMemoryGameRepository implements GameRepository {

    private final Map<String, Game> database = new HashMap<>();

    @Override
    public Optional<Game> findById(String id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Game> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public List<Game> findByState(GameState state) {
        return database.values().stream()
                .filter(game -> game.getState() == state)
                .collect(Collectors.toList());
    }

    @Override
    public List<Game> findByPointIdAndState(String pointId, GameState state) {
        return database.values().stream()
                .filter(game -> game.getState() == state && pointId.equals(game.getPointId()))
                .collect(Collectors.toList());
    }

    @Override
    public Game save(Game game) {
        if (game == null) {
            return null;
        }
        if (game.getId() == null) {
            game.setId("GAME_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        database.put(game.getId(), game);
        return game;
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
