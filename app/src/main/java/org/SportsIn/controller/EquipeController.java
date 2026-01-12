package org.SportsIn.controller;

import org.SportsIn.model.user.Equipe;
import org.SportsIn.repository.EquipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/equipes")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class EquipeController {

    @Autowired
    private EquipeRepository equipeRepository;

    @GetMapping
    public ResponseEntity<List<Equipe>> getAll() {
        return ResponseEntity.ok(equipeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipe> getById(@NonNull @PathVariable Long id) {
        Optional<Equipe> equipe = equipeRepository.findById(id);
        return equipe.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Equipe> create(@NonNull @RequestBody Equipe equipe) {
        Equipe saved = equipeRepository.save(equipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Equipe> update(@NonNull @PathVariable Long id, @NonNull @RequestBody Equipe equipeDetails) {
        Optional<Equipe> equipeOpt = equipeRepository.findById(id);
        if (equipeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Equipe equipe = equipeOpt.get();
        equipe.setNom(equipeDetails.getNom());
        return ResponseEntity.ok(equipeRepository.save(equipe));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@NonNull @PathVariable Long id) {
        if (!equipeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        equipeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
