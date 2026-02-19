package org.SportsIn.controller;

import org.SportsIn.model.chat.Message;
import org.SportsIn.services.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Récupère tous les messages d'une équipe
     */
    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<List<Message>> getByEquipe(@PathVariable Long equipeId) {
        return ResponseEntity.ok(messageService.getByEquipe(equipeId));
    }

    /**
     * Envoie un nouveau message dans le chat d'équipe
     * Body: { "joueurId": 1, "equipeId": 2, "contenu": "Hello!" }
     */
    @PostMapping
    public ResponseEntity<Message> send(@RequestBody Map<String, Object> body) {
        Long joueurId = Long.valueOf(body.get("joueurId").toString());
        Long equipeId = Long.valueOf(body.get("equipeId").toString());
        String contenu = (String) body.get("contenu");

        if (joueurId == null || equipeId == null || contenu == null || contenu.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return messageService.send(joueurId, equipeId, contenu)
                .map(msg -> ResponseEntity.status(HttpStatus.CREATED).body(msg))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Supprime un message (pour tout le monde)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (messageService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
