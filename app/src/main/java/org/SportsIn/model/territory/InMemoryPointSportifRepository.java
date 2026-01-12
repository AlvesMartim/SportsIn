package org.SportsIn.model.territory;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryPointSportifRepository implements PointSportifRepository {

    private final Map<Long, PointSportif> database = new HashMap<>();

    @Override
    public Optional<PointSportif> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<PointSportif> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public void save(PointSportif point) {
        if (point == null || point.getId() == null) {
            return;
        }
        database.put(point.getId(), point);
    }
}
