package org.SportsIn.controller;

import org.SportsIn.model.territory.Zone;
import org.SportsIn.services.ZoneService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @GetMapping
    public ResponseEntity<List<Zone>> getAll() {
        return ResponseEntity.ok(zoneService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Zone> getById(@NonNull @PathVariable Long id) {
        return zoneService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/arene/{areneId}")
    public ResponseEntity<List<Zone>> getByAreneId(@NonNull @PathVariable String areneId) {
        return ResponseEntity.ok(zoneService.getZonesByAreneId(areneId));
    }
}
