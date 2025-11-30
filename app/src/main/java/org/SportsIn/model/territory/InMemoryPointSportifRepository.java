package org.SportsIn.model.territory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryPointSportifRepository implements PointSportifRepository {

    private final Map<Long, PointSportif> database = new HashMap<>();

    @Override
    public Optional<PointSportif> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public void save(PointSportif point) {
        if (point == null || point.getId() == null) {
            return;
        }
        database.put(point.getId(), point);
    }
}
