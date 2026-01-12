package org.SportsIn.controller;

import org.SportsIn.model.user.Joueur;
import org.SportsIn.repository.JoueurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/joueurs")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class JoueurController {

    @Autowired
    private JoueurRepository joueurRepository;

    @GetMapping
    public ResponseEntity<List<Joueur>> getAll() {
        return ResponseEntity.ok(joueurRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Joueur> getById(@NonNull @PathVariable Long id) {
        Optional<Joueur> joueur = joueurRepository.findById(id);
        return joueur.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<List<Joueur>> getByEquipe(@NonNull @PathVariable Long equipeId) {
        return ResponseEntity.ok(joueurRepository.findByEquipeId(equipeId));
    }

    @PostMapping
    public ResponseEntity<Joueur> create(@NonNull @RequestBody Joueur joueur) {
        Joueur saved = joueurRepository.save(joueur);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Joueur> update(@NonNull @PathVariable Long id, @NonNull @RequestBody Joueur joueurDetails) {
        Optional<Joueur> joueurOpt = joueurRepository.findById(id);
        if (joueurOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Joueur joueur = joueurOpt.get();
        joueur.setPseudo(joueurDetails.getPseudo());
        joueur.setEquipe(joueurDetails.getEquipe());
        return ResponseEntity.ok(joueurRepository.save(joueur));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@NonNull @PathVariable Long id) {
        if (!joueurRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        joueurRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
