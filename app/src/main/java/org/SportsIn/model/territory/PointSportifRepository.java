package org.SportsIn.model.territory;

import java.util.Optional;

public interface PointSportifRepository {
    Optional<PointSportif> findById(Long id);
    void save(PointSportif point);
}
