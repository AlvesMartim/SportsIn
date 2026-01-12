package org.SportsIn.model.territory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryZoneRepository implements ZoneRepository {

    private final Map<Long, Zone> database = new HashMap<>();

    @Override
    public Optional<Zone> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Zone> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public void save(Zone zone) {
        if (zone == null || zone.getId() == null) {
            return;
        }
        database.put(zone.getId(), zone);
    }

    @Override
    public List<Zone> findZonesByPointId(Long pointId) {
        return database.values().stream()
                .filter(zone -> zone.getPoints().stream()
                        .anyMatch(p -> p.getId().equals(pointId)))
                .collect(Collectors.toList());
    }
}
