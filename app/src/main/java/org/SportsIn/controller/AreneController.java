package org.SportsIn.controller;

import org.SportsIn.model.Arene;
import org.SportsIn.repository.AreneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/arenes")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AreneController {

    @Autowired
    private AreneRepository areneRepository;

    @GetMapping
    public ResponseEntity<List<Arene>> getAll() {
        return ResponseEntity.ok(areneRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Arene> getById(@NonNull @PathVariable String id) {
        Optional<Arene> arene = areneRepository.findById(id);
        return arene.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<List<Arene>> getByEquipe(@NonNull @PathVariable Long equipeId) {
        return ResponseEntity.ok(areneRepository.findByControllingTeamId(equipeId));
    }

    @GetMapping("/sport/{sport}")
    public ResponseEntity<List<Arene>> getBySport(@NonNull @PathVariable String sport) {
        return ResponseEntity.ok(areneRepository.findBySportsDisponiblesContaining(sport));
    }

    @PostMapping
    public ResponseEntity<Arene> create(@NonNull @RequestBody Arene arene) {
        Arene saved = areneRepository.save(arene);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Arene> update(@NonNull @PathVariable String id, @NonNull @RequestBody Arene areneDetails) {
        Optional<Arene> areneOpt = areneRepository.findById(id);
        if (areneOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Arene arene = areneOpt.get();
        arene.setNom(areneDetails.getNom());
        arene.setLatitude(areneDetails.getLatitude());
        arene.setLongitude(areneDetails.getLongitude());
        arene.setControllingTeam(areneDetails.getControllingTeam());
        arene.setSportsDisponibles(areneDetails.getSportsDisponibles());
        return ResponseEntity.ok(areneRepository.save(arene));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@NonNull @PathVariable String id) {
        if (!areneRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        areneRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
