package org.SportsIn.controller;

import org.SportsIn.model.user.Joueur;
import org.SportsIn.services.JoueurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/joueurs")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class JoueurController {

    private final JoueurService joueurService;

    public JoueurController(JoueurService joueurService) {
        this.joueurService = joueurService;
    }

    @GetMapping
    public ResponseEntity<List<Joueur>> getAll() {
        return ResponseEntity.ok(joueurService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Joueur> getById(@NonNull @PathVariable Long id) {
        return joueurService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<List<Joueur>> getByEquipe(@NonNull @PathVariable Long equipeId) {
        return ResponseEntity.ok(joueurService.getByEquipe(equipeId));
    }

    @PostMapping
    public ResponseEntity<Joueur> create(@NonNull @RequestBody Joueur joueur) {
        Joueur saved = joueurService.create(joueur);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Joueur> update(@NonNull @PathVariable Long id, @NonNull @RequestBody Joueur joueurDetails) {
        return joueurService.update(id, joueurDetails)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@NonNull @PathVariable Long id) {
        if (joueurService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
