package org.SportsIn.model.territory;

import java.util.List;
import java.util.Optional;

public interface PointSportifRepository {
    Optional<PointSportif> findById(Long id);
    List<PointSportif> findAll();
    void save(PointSportif point);
}
