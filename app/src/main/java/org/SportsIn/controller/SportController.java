package org.SportsIn.controller;

import org.SportsIn.model.Sport;
import org.SportsIn.repository.SportRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sports")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class SportController {

    private final SportRepository sportRepository;

    public SportController(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
    }

    @GetMapping
    public ResponseEntity<List<Sport>> getAll() {
        return ResponseEntity.ok(sportRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sport> getById(@PathVariable Long id) {
        return sportRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Sport> getByCode(@PathVariable String code) {
        return sportRepository.findByCode(code)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
