package org.SportsIn.controller;

import org.SportsIn.model.Session;
import org.SportsIn.model.SessionRepository;
import org.SportsIn.model.SessionState;
import org.SportsIn.services.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class SessionController {

    private final SessionRepository sessionRepository;
    private final SessionService sessionService;

    public SessionController(SessionRepository sessionRepository, SessionService sessionService) {
        this.sessionRepository = sessionRepository;
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<List<Session>> getAll() {
        return ResponseEntity.ok(sessionRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Session> getById(@NonNull @PathVariable String id) {
        return sessionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Session>> getActive() {
        return ResponseEntity.ok(sessionRepository.findByState(SessionState.ACTIVE));
    }

    @PostMapping
    public ResponseEntity<Session> create(@NonNull @RequestBody Session session) {
        session.setState(SessionState.ACTIVE);
        session.setCreatedAt(LocalDateTime.now());
        Session saved = sessionRepository.save(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Session> update(@NonNull @PathVariable String id, @NonNull @RequestBody Session sessionDetails) {
        return sessionRepository.findById(id)
                .map(session -> {
                    session.setSport(sessionDetails.getSport());
                    session.setPointId(sessionDetails.getPointId());
                    session.setParticipants(sessionDetails.getParticipants());
                    session.setResult(sessionDetails.getResult());
                    return ResponseEntity.ok(sessionRepository.save(session));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@NonNull @PathVariable String id) {
        if (sessionRepository.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/terminate")
    public ResponseEntity<Session> terminate(@NonNull @PathVariable String id) {
        return sessionRepository.findById(id)
                .map(session -> {
                    sessionService.processSessionCompletion(id);
                    return ResponseEntity.ok(sessionRepository.findById(id).orElse(session));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
