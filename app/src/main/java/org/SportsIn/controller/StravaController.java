package org.SportsIn.controller;

import org.SportsIn.config.StravaProperties;
import org.SportsIn.dto.StravaSyncResultDTO;
import org.SportsIn.model.strava.StravaAccount;
import org.SportsIn.model.strava.StravaActivity;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.repository.JoueurRepository;
import org.SportsIn.repository.StravaAccountRepository;
import org.SportsIn.repository.StravaActivityRepository;
import org.SportsIn.services.StravaApiClient;
import org.SportsIn.services.StravaLeaderboardService;
import org.SportsIn.services.StravaOAuthService;
import org.SportsIn.services.StravaSyncService;
import org.SportsIn.services.StravaWebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/strava")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class StravaController {

    private final StravaOAuthService oauthService;
    private final StravaSyncService syncService;
    private final StravaWebhookService webhookService;
    private final StravaActivityRepository activityRepo;
    private final StravaAccountRepository accountRepo;
    private final JoueurRepository joueurRepo;
    private final EquipeRepository equipeRepo;
    private final StravaLeaderboardService leaderboardService;
    private final StravaProperties props;

    public StravaController(StravaOAuthService oauthService,
                            StravaSyncService syncService,
                            StravaWebhookService webhookService,
                            StravaActivityRepository activityRepo,
                            StravaAccountRepository accountRepo,
                            JoueurRepository joueurRepo,
                            EquipeRepository equipeRepo,
                            StravaLeaderboardService leaderboardService,
                            StravaProperties props) {
        this.oauthService = oauthService;
        this.leaderboardService = leaderboardService;
        this.syncService = syncService;
        this.webhookService = webhookService;
        this.activityRepo = activityRepo;
        this.accountRepo = accountRepo;
        this.joueurRepo = joueurRepo;
        this.equipeRepo = equipeRepo;
        this.props = props;
    }

    // ----------------------------------------------------------------
    // OAuth2 Flow
    // ----------------------------------------------------------------

    /**
     * GET /api/strava/auth/url?joueurId=<id>
     * Retourne l'URL d'autorisation Strava à laquelle rediriger l'utilisateur.
     */
    @GetMapping("/auth/url")
    public ResponseEntity<?> getAuthUrl(@RequestParam Long joueurId) {
        if (props.getClientId() == null || props.getClientId().isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(errorResponse("STRAVA_CLIENT_ID non configuré"));
        }
        String url = oauthService.buildAuthorizationUrl(joueurId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("url", url);
        return ResponseEntity.ok(resp);
    }

    /**
     * GET /api/strava/callback?code=<code>&state=<joueurId>&scope=<scopes>
     * Callback OAuth2 Strava. Échange le code et redirige vers le frontend.
     */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam String code,
                                         @RequestParam String state,
                                         @RequestParam(required = false) String error) {
        String frontendBase = props.getFrontendUrl() + "/strava";

        if (error != null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendBase + "?error=" + error))
                    .build();
        }

        try {
            Long joueurId = Long.parseLong(state);
            oauthService.handleCallback(code, joueurId);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendBase + "?connected=true"))
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendBase + "?error=callback_failed"))
                    .build();
        }
    }

    /**
     * GET /api/strava/status?joueurId=<id>
     * Statut de connexion Strava du joueur.
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@RequestParam Long joueurId) {
        boolean connected = oauthService.isConnected(joueurId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("connected", connected);
        if (connected) {
            oauthService.getAccount(joueurId).ifPresent(acc -> {
                resp.put("stravaAthleteId", acc.getStravaAthleteId());
                resp.put("connectedAt", acc.getConnectedAt());
                resp.put("tokenExpiresAt", acc.getTokenExpiresAt());
            });
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * DELETE /api/strava/disconnect?joueurId=<id>
     * Déconnecte le compte Strava (suppression des tokens, RGPD).
     */
    @DeleteMapping("/disconnect")
    public ResponseEntity<?> disconnect(@RequestParam Long joueurId) {
        if (!oauthService.isConnected(joueurId)) {
            return ResponseEntity.notFound().build();
        }
        oauthService.disconnect(joueurId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Compte Strava déconnecté avec succès");
        return ResponseEntity.ok(resp);
    }

    // ----------------------------------------------------------------
    // Synchronisation
    // ----------------------------------------------------------------

    /**
     * POST /api/strava/sync?joueurId=<id>
     * Synchronisation manuelle des activités récentes.
     */
    @PostMapping("/sync")
    public ResponseEntity<?> syncNow(@RequestParam Long joueurId) {
        if (!oauthService.isConnected(joueurId)) {
            return ResponseEntity.badRequest().body(errorResponse("Compte Strava non connecté"));
        }
        try {
            StravaSyncResultDTO result = syncService.syncForJoueur(joueurId);
            return ResponseEntity.ok(result);
        } catch (StravaApiClient.StravaRateLimitException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Erreur de synchronisation: " + e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    // Webhook Strava
    // ----------------------------------------------------------------

    /**
     * GET /api/strava/webhook
     * Vérification du challenge Strava lors de l'abonnement au webhook.
     */
    @GetMapping("/webhook")
    public ResponseEntity<?> webhookVerify(@RequestParam("hub.mode") String mode,
                                           @RequestParam("hub.challenge") String challenge,
                                           @RequestParam("hub.verify_token") String verifyToken) {
        if (!"subscribe".equals(mode) || !props.getWebhookVerifyToken().equals(verifyToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse("Token de vérification invalide"));
        }
        Map<String, String> resp = new HashMap<>();
        resp.put("hub.challenge", challenge);
        return ResponseEntity.ok(resp);
    }

    /**
     * POST /api/strava/webhook
     * Réception d'un événement Strava (nouvelle activité, etc.).
     * Répond immédiatement 200 à Strava et traite l'événement de façon asynchrone.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhookEvent(@RequestBody Map<String, Object> payload) {
        try {
            String objectType = (String) payload.get("object_type");
            String aspectType = (String) payload.get("aspect_type");
            long objectId = toLong(payload.get("object_id"));
            long ownerId = toLong(payload.get("owner_id"));
            webhookService.handleWebhookAsync(objectType, aspectType, objectId, ownerId);
        } catch (Exception e) {
            System.err.println("Webhook Strava - erreur parsing payload: " + e.getMessage());
        }
        // Toujours répondre 200 immédiatement (Strava exige < 2s)
        return ResponseEntity.ok().build();
    }

    // ----------------------------------------------------------------
    // Activités importées
    // ----------------------------------------------------------------

    /**
     * GET /api/strava/activities?joueurId=<id>
     * Liste les activités Strava importées pour un joueur.
     */
    @GetMapping("/activities")
    public ResponseEntity<?> getActivities(@RequestParam Long joueurId) {
        List<StravaActivity> activities = activityRepo.findByJoueurIdOrderByStartDateDesc(joueurId);
        return ResponseEntity.ok(activities);
    }

    /**
     * GET /api/strava/activities/team?equipeId=<id>
     * Liste les activités Strava de toute l'équipe (pour la carte polylines).
     */
    @GetMapping("/activities/team")
    public ResponseEntity<?> getTeamActivities(@RequestParam Long equipeId) {
        List<StravaActivity> activities = activityRepo.findByEquipeIdOrderByStartDateDesc(equipeId);
        return ResponseEntity.ok(activities);
    }

    // ----------------------------------------------------------------
    // Statistiques
    // ----------------------------------------------------------------

    /**
     * GET /api/strava/stats/player?joueurId=<id>
     * Statistiques agrégées Strava pour un joueur.
     */
    @GetMapping("/stats/player")
    public ResponseEntity<?> getPlayerStats(@RequestParam Long joueurId) {
        List<StravaActivity> activities =
                activityRepo.findByJoueurIdAndAntiCheatFlaggedFalseOrderByStartDateDesc(joueurId);

        double totalDistance = activities.stream().mapToDouble(StravaActivity::getDistanceMeters).sum();
        double totalElevation = activities.stream().mapToDouble(StravaActivity::getTotalElevationGain).sum();
        double totalInfluence = activities.stream().mapToDouble(StravaActivity::getInfluenceGranted).sum();
        long totalActivities = activities.size();

        Map<String, Long> bySport = activities.stream()
                .collect(java.util.stream.Collectors.groupingBy(StravaActivity::getSportType,
                        java.util.stream.Collectors.counting()));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDistanceKm", Math.round(totalDistance / 10.0) / 100.0);
        stats.put("totalElevationGainM", totalElevation);
        stats.put("totalActivities", totalActivities);
        stats.put("totalInfluenceGranted", totalInfluence);
        stats.put("activitiesBySport", bySport);
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/strava/stats/leaderboard?period=weekly|monthly&zoneId=<id>
     * Classement des équipes par distance Strava, avec décomposition par sport.
     * Le paramètre zoneId (optionnel) filtre sur les activités traversant cette zone.
     */
    @GetMapping("/stats/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @RequestParam(defaultValue = "weekly") String period,
            @RequestParam(required = false) Long zoneId) {
        List<Map<String, Object>> leaderboard = leaderboardService.compute(period, zoneId);
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * GET /api/strava/rate-limit
     * Statut du rate limiter Strava (pour debug/admin).
     */
    @GetMapping("/rate-limit")
    public ResponseEntity<?> getRateLimitStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("limitPer15Min", props.getRateLimitPer15Min());
        status.put("limitPerDay", props.getRateLimitPerDay());
        return ResponseEntity.ok(status);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private String computeSinceDate(String period) {
        // Instant ne supporte pas WEEKS/MONTHS → on convertit en jours
        long days = "monthly".equals(period) ? 30L : 7L;
        return java.time.Instant.now().minus(days, java.time.temporal.ChronoUnit.DAYS).toString();
    }

    private long toLong(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value instanceof String s) return Long.parseLong(s);
        throw new IllegalArgumentException("Impossible de convertir en long: " + value);
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("error", message);
        return resp;
    }
}
