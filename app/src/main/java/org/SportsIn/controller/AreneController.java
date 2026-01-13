package org.SportsIn.controller;

import org.SportsIn.model.Arene;
import org.SportsIn.services.AreneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/arenes")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AreneController {

    private final AreneService areneService;

    public AreneController(AreneService areneService) {
        this.areneService = areneService;
    }

    @GetMapping
    public ResponseEntity<List<Arene>> getAll() {
        return ResponseEntity.ok(areneService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Arene> getById(@NonNull @PathVariable String id) {
        return areneService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<List<Arene>> getByEquipe(@NonNull @PathVariable Long equipeId) {
        return ResponseEntity.ok(areneService.getByEquipe(equipeId));
    }

    @GetMapping("/sport/{sport}")
    public ResponseEntity<List<Arene>> getBySport(@NonNull @PathVariable String sport) {
        return ResponseEntity.ok(areneService.getBySport(sport));
    }

    @PostMapping
    public ResponseEntity<Arene> create(@NonNull @RequestBody Arene arene) {
        Arene saved = areneService.create(arene);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Arene> update(@NonNull @PathVariable String id, @NonNull @RequestBody Arene areneDetails) {
        return areneService.update(id, areneDetails)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@NonNull @PathVariable String id) {
        if (areneService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
