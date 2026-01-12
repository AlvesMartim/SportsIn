package org.SportsIn.model.territory;

import java.util.List;
import java.util.Optional;

public interface RouteRepository {
    Optional<Route> findById(Long id);
    List<Route> findAll();
    void save(Route route);
    void saveAll(List<Route> routes);
    void deleteAll();
}
