package org.SportsIn.model.territory;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryRouteRepository implements RouteRepository {

    private final Map<Long, Route> database = new HashMap<>();

    @Override
    public Optional<Route> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Route> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public void save(Route route) {
        if (route == null || route.getId() == null) {
            return;
        }
        database.put(route.getId(), route);
    }

    @Override
    public void saveAll(List<Route> routes) {
        if (routes == null) return;
        for (Route route : routes) {
            save(route);
        }
    }

    @Override
    public void deleteAll() {
        database.clear();
    }
}
