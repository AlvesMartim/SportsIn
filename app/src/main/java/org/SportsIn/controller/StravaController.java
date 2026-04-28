package org.SportsIn.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.SportsIn.config.StravaProperties;
import org.SportsIn.model.strava.StravaActivity;
import org.SportsIn.repository.JoueurStravaDataRepository;
import org.SportsIn.services.strava.StravaActivityService;
import org.SportsIn.services.strava.StravaOAuthService;
import org.SportsIn.services.strava.StravaPointCalculator;
import org.SportsIn.services.strava.StravaSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoints REST pour l'intégration Strava.
 * Gère le flux OAuth, les imports d'activités et les webhooks Strava.
 */
@RestController
@RequestMapping("/api/strava")
public class StravaController {

    private static final Logger log = LoggerFactory.getLogger(StravaController.class);

    private final StravaProperties properties;
    private final StravaOAuthService oAuthService;
    private final StravaActivityService activityService;
    private final StravaSessionService sessionService;
    private final JoueurStravaDataRepository stravaDataRepository;

    public StravaController(StravaProperties properties,
                            StravaOAuthService oAuthService,
                            StravaActivityService activityService,
                            StravaSessionService sessionService,
                            JoueurStravaDataRepository stravaDataRepository) {
        this.properties = properties;
        this.oAuthService = oAuthService;
        this.activityService = activityService;
        this.sessionService = sessionService;
        this.stravaDataRepository = stravaDataRepository;
    }

    // =========================================================
    // OAuth
    // =========================================================

    /**
     * GET /api/strava/connect?joueurId=X
     * Redirige le joueur vers la page d'autorisation Strava.
     */
    @GetMapping("/connect")
    public void connect(@RequestParam Long joueurId, HttpServletResponse response) throws IOException {
        if (!properties.isConfigured()) {
            response.sendRedirect(properties.getFrontendBaseUrl() + "/profile?strava=not_configured");
            return;
        }
        String authUrl = oAuthService.buildAuthorizationUrl(joueurId);
        response.sendRedirect(authUrl);
    }

    /**
     * GET /api/strava/callback?code=...&state=joueurId
     * Reçoit le code OAuth de Strava, échange les tokens, redirige vers le frontend.
     */
    @GetMapping("/callback")
    public void callback(@RequestParam(required = false) String code,
                         @RequestParam(required = false) String state,
                         @RequestParam(required = false) String error,
                         HttpServletResponse response) throws IOException {
        String frontendProfile = properties.getFrontendBaseUrl() + "/profile";

        if (error != null) {
            log.warn("Callback Strava avec erreur : {}", error);
            response.sendRedirect(frontendProfile + "?strava=denied");
            return;
        }
        if (code == null || state == null) {
            response.sendRedirect(frontendProfile + "?strava=error");
            return;
        }

        try {
            Long joueurId = Long.parseLong(state);
            oAuthService.processCallback(code, joueurId);
            response.sendRedirect(frontendProfile + "?strava=success");
        } catch (Exception e) {
            log.error("Erreur lors du callback Strava : {}", e.getMessage());
            response.sendRedirect(frontendProfile + "?strava=error");
        }
    }

    // =========================================================
    // Statut et activités
    // =========================================================

    /**
     * GET /api/strava/status?joueurId=X
     * Retourne si le joueur est connecté à Strava.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(@RequestParam Long joueurId) {
        Map<String, Object> result = new HashMap<>();
        stravaDataRepository.findByJoueurId(joueurId).ifPresentOrElse(data -> {
            result.put("connected", data.isStravaConnected());
            if (data.isStravaConnected()) {
                result.put("athleteId", data.getStravaAthleteId());
            }
        }, () -> result.put("connected", false));
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/strava/activities?joueurId=X
     * Liste les activités Strava sauvegardées localement pour ce joueur.
     */
    @GetMapping("/activities")
    public ResponseEntity<?> getActivities(@RequestParam Long joueurId) {
        try {
            List<StravaActivity> activities = activityService.getActivitiesForJoueur(joueurId);
            return ResponseEntity.ok(activities.stream().map(this::toResponse).toList());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    /**
     * POST /api/strava/import-latest?joueurId=X
     * Importe la dernière activité Strava non encore importée.
     */
    @PostMapping("/import-latest")
    public ResponseEntity<?> importLatest(@RequestParam Long joueurId) {
        try {
            StravaActivity activity = sessionService.importLatestActivity(joueurId);
            return ResponseEntity.ok(toResponse(activity));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de l'import Strava pour le joueur {} : {}", joueurId, e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse("Erreur lors de l'import : " + e.getMessage()));
        }
    }

    /**
     * POST /api/strava/import/{stravaActivityId}?joueurId=X
     * Importe une activité Strava précise.
     */
    @PostMapping("/import/{stravaActivityId}")
    public ResponseEntity<?> importById(@PathVariable Long stravaActivityId,
                                        @RequestParam Long joueurId) {
        try {
            StravaActivity activity = sessionService.importActivityById(joueurId, stravaActivityId);
            return ResponseEntity.ok(toResponse(activity));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur lors de l'import de l'activité {} pour le joueur {} : {}", stravaActivityId, joueurId, e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse("Erreur lors de l'import : " + e.getMessage()));
        }
    }

    /**
     * POST /api/strava/disconnect?joueurId=X
     * Déconnecte le joueur de Strava (supprime les tokens).
     */
    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect(@RequestParam Long joueurId) {
        oAuthService.disconnect(joueurId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // =========================================================
    // Webhook Strava
    // =========================================================

    /**
     * GET /api/strava/webhook
     * Endpoint de vérification d'abonnement Strava.
     * Strava envoie hub.mode=subscribe + hub.challenge + hub.verify_token.
     */
    @GetMapping("/webhook")
    public ResponseEntity<?> webhookVerify(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.challenge", required = false) String challenge,
            @RequestParam(name = "hub.verify_token", required = false) String verifyToken) {

        if ("subscribe".equals(mode) && properties.getVerifyToken().equals(verifyToken)) {
            return ResponseEntity.ok(Map.of("hub.challenge", challenge));
        }
        log.warn("Tentative de vérification webhook Strava invalide (mode={}, token={})", mode, verifyToken);
        return ResponseEntity.status(403).body(errorResponse("Token de vérification invalide"));
    }

    /**
     * POST /api/strava/webhook
     * Reçoit les événements Strava en temps réel.
     * Doit répondre 200 OK rapidement (< 2 secondes).
     *
     * TODO: déplacer le traitement dans un @Async pour respecter cette contrainte
     *       en cas de charge ou de latence de la base de données.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhookEvent(@RequestBody Map<String, Object> event) {
        try {
            String objectType = (String) event.get("object_type");
            String aspectType = (String) event.get("aspect_type");

            if ("activity".equals(objectType) && "create".equals(aspectType)) {
                Object ownerIdRaw = event.get("owner_id");
                Object activityIdRaw = event.get("object_id");

                if (ownerIdRaw instanceof Number ownerNum && activityIdRaw instanceof Number actNum) {
                    Long stravaAthleteId = ownerNum.longValue();
                    Long stravaActivityId = actNum.longValue();

                    // Trouver le joueur par son athlete_id Strava et importer l'activité
                    stravaDataRepository.findByStravaAthleteId(stravaAthleteId).ifPresent(stravaData -> {
                        try {
                            sessionService.importActivityById(stravaData.getJoueurId(), stravaActivityId);
                            log.info("Webhook Strava : activité {} importée pour l'athlète {}",
                                stravaActivityId, stravaAthleteId);
                        } catch (Exception e) {
                            log.warn("Webhook Strava : impossible d'importer l'activité {} : {}",
                                stravaActivityId, e.getMessage());
                        }
                    });
                }
            }
        } catch (Exception e) {
            // Toujours répondre 200 à Strava même en cas d'erreur interne
            log.error("Erreur lors du traitement du webhook Strava : {}", e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    // =========================================================
    // Helpers
    // =========================================================

    private Map<String, Object> toResponse(StravaActivity a) {
        Map<String, Object> r = new HashMap<>();
        r.put("id", a.getId());
        r.put("stravaActivityId", a.getStravaActivityId());
        r.put("stravaType", a.getStravaType());
        r.put("sportCode", a.getSportCode());
        r.put("name", a.getName());
        r.put("startDate", a.getStartDate());
        r.put("distanceMeters", a.getDistanceMeters());
        r.put("movingTimeSeconds", a.getMovingTimeSeconds());
        r.put("elevationGain", a.getElevationGain());
        r.put("importedAsSession", a.isImportedAsSession());
        r.put("pointsAttribues", StravaPointCalculator.calculate(
            a.getDistanceMeters(), a.getMovingTimeSeconds(), a.getElevationGain()));
        return r;
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> r = new HashMap<>();
        r.put("success", false);
        r.put("error", message);
        return r;
    }
}
