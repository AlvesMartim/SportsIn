package org.SportsIn.controller;

import org.SportsIn.model.user.Joueur;
import org.SportsIn.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Inscription d'un nouveau joueur
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Validation basique
            if (request.pseudo == null || request.pseudo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(errorResponse("Le pseudo est requis"));
            }
            if (request.email == null || request.email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(errorResponse("L'email est requis"));
            }
            if (request.password == null || request.password.length() < 6) {
                return ResponseEntity.badRequest().body(errorResponse("Le mot de passe doit contenir au moins 6 caractères"));
            }

            Joueur joueur = authService.register(request.pseudo.trim(), request.email.trim().toLowerCase(), request.password);

            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse(joueur));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    /**
     * Connexion d'un joueur
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            if (request.email == null || request.email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(errorResponse("L'email est requis"));
            }
            if (request.password == null || request.password.isEmpty()) {
                return ResponseEntity.badRequest().body(errorResponse("Le mot de passe est requis"));
            }

            Optional<Joueur> joueurOpt = authService.login(request.email.trim().toLowerCase(), request.password);

            if (joueurOpt.isPresent()) {
                return ResponseEntity.ok(authResponse(joueurOpt.get()));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse("Email ou mot de passe incorrect"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse("Erreur lors de la connexion"));
        }
    }

    /**
     * Récupérer le profil de l'utilisateur connecté
     * GET /api/auth/me
     */
    @GetMapping("/me/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        Optional<Joueur> joueurOpt = authService.getById(id);

        if (joueurOpt.isPresent()) {
            return ResponseEntity.ok(userResponse(joueurOpt.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // --- DTOs internes ---

    static class RegisterRequest {
        public String pseudo;
        public String email;
        public String password;
    }

    static class LoginRequest {
        public String email;
        public String password;
    }

    // --- Helpers pour les réponses ---

    private Map<String, Object> authResponse(Joueur joueur) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", "TOKEN_" + joueur.getId() + "_" + System.currentTimeMillis());
        response.put("user", userResponse(joueur));
        return response;
    }

    private Map<String, Object> userResponse(Joueur joueur) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", joueur.getId());
        user.put("pseudo", joueur.getPseudo());
        user.put("email", joueur.getEmail());
        user.put("equipeId", joueur.getEquipe() != null ? joueur.getEquipe().getId() : null);
        return user;
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }
}
