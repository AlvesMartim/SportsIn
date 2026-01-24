package org.SportsIn.services;

import org.SportsIn.model.territory.Zone;
import org.SportsIn.model.territory.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;

    public ZoneService(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    public List<Zone> getAll() {
        return zoneRepository.findAll();
    }

    public Optional<Zone> getById(Long id) {
        return zoneRepository.findById(id);
    }

    public List<Zone> getZonesByPointId(Long pointId) {
        return zoneRepository.findZonesByPointId(pointId);
    }
}
