package org.SportsIn.model.territory;

import java.util.List;
import java.util.Optional;

public interface ZoneRepository {
    Optional<Zone> findById(Long id);
    List<Zone> findAll();
    void save(Zone zone);
    
    /**
     * Trouve la ou les zones qui contiennent cette arène spécifique.
     * Une arène pourrait théoriquement appartenir à plusieurs zones (chevauchement),
     * ou une seule.
     */
    List<Zone> findZonesByAreneId(String areneId);
}
