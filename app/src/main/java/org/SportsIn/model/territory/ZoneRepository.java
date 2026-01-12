package org.SportsIn.model.territory;

import java.util.List;
import java.util.Optional;

public interface ZoneRepository {
    Optional<Zone> findById(Long id);
    List<Zone> findAll();
    void save(Zone zone);
    
    /**
     * Trouve la ou les zones qui contiennent ce point spécifique.
     * Un point pourrait théoriquement appartenir à plusieurs zones (chevauchement),
     * ou une seule.
     */
    List<Zone> findZonesByPointId(Long pointId);
}
