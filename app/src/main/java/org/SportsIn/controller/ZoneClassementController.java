package org.SportsIn.controller;

import org.SportsIn.model.territory.ZoneDepartement;
import org.SportsIn.repository.AreneRepository;
import org.SportsIn.repository.ZoneDepartementRepository;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/zones")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ZoneClassementController {

    private final ZoneDepartementRepository zoneDeptRepo;
    private final AreneRepository areneRepo;

    public ZoneClassementController(ZoneDepartementRepository zoneDeptRepo,
                                    AreneRepository areneRepo) {
        this.zoneDeptRepo = zoneDeptRepo;
        this.areneRepo = areneRepo;
    }

    @GetMapping("/classement")
    public List<Map<String, Object>> getClassement() {
        List<ZoneDepartement> zones = zoneDeptRepo.findAllOrderByInfluenceDesc();

        return zones.stream().map(zone -> {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("code", zone.getCode());
            dto.put("nom", zone.getNom());
            dto.put("totalInfluence", zone.getTotalInfluence());
            dto.put("controllingTeamId", zone.getControllingTeamId());
            dto.put("controllingTeamNom",
                    zone.getControllingTeam() != null ? zone.getControllingTeam().getNom() : null);
            long nbArenes = areneRepo.countByDepartement(zone.getCode());
            dto.put("nbArenes", nbArenes);
            return dto;
        }).collect(Collectors.toList());
    }
}
