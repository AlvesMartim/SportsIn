package org.SportsIn.controller;

import org.SportsIn.model.progression.ActivePerk;
import org.SportsIn.model.progression.LevelThreshold;
import org.SportsIn.model.progression.PerkDefinition;
import org.SportsIn.model.user.Equipe;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.services.PerkActivationService;
import org.SportsIn.services.TeamProgressionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProgressionController {

    private final EquipeRepository equipeRepository;
    private final TeamProgressionService progressionService;
    private final PerkActivationService perkActivationService;

    public ProgressionController(EquipeRepository equipeRepository,
                                 TeamProgressionService progressionService,
                                 PerkActivationService perkActivationService) {
        this.equipeRepository = equipeRepository;
        this.progressionService = progressionService;
        this.perkActivationService = perkActivationService;
    }

    /**
     * GET /api/teams/{teamId}/progression
     * Retourne le niveau, XP, perks debloqués et perks actifs d'une équipe.
     */
    @GetMapping("/teams/{teamId}/progression")
    public ResponseEntity<?> getProgression(@PathVariable Long teamId) {
        Equipe equipe = equipeRepository.findById(teamId).orElse(null);
        if (equipe == null) return ResponseEntity.notFound().build();

        int level = progressionService.getLevel(equipe);
        List<PerkDefinition> unlocked = progressionService.getUnlockedPerks(equipe);
        List<ActivePerk> activePerks = perkActivationService.getActivePerksForTeam(teamId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("teamId", teamId);
        response.put("teamName", equipe.getNom());
        response.put("xp", equipe.getXp());
        response.put("level", level);
        response.put("xpForNextLevel", LevelThreshold.xpForNextLevel(equipe.getXp()));
        response.put("maxLevel", LevelThreshold.getMaxLevel());
        response.put("unlockedPerks", unlocked);
        response.put("activePerks", activePerks);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/perks
     * Retourne le catalogue complet des perks.
     */
    @GetMapping("/perks")
    public ResponseEntity<List<PerkDefinition>> getAllPerks() {
        return ResponseEntity.ok(progressionService.getAllPerkDefinitions());
    }

    /**
     * POST /api/teams/{teamId}/perks/activate
     * Active un perk pour une equipe.
     * Body: { "perkCode": "SHIELD_QUARTIER", "targetId": "123" }
     */
    @PostMapping("/teams/{teamId}/perks/activate")
    public ResponseEntity<?> activatePerk(@PathVariable Long teamId,
                                          @RequestBody Map<String, String> body) {
        String perkCode = body.get("perkCode");
        String targetId = body.get("targetId");

        if (perkCode == null || perkCode.isBlank()) {
            return ResponseEntity.badRequest().body("perkCode est requis");
        }

        if (!equipeRepository.existsById(teamId)) {
            return ResponseEntity.notFound().build();
        }

        try {
            ActivePerk activated = perkActivationService.activatePerk(teamId, perkCode, targetId);
            return ResponseEntity.ok(activated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/teams/{teamId}/perks/active
     * Retourne les perks actifs d'une equipe.
     */
    @GetMapping("/teams/{teamId}/perks/active")
    public ResponseEntity<?> getActivePerks(@PathVariable Long teamId) {
        if (!equipeRepository.existsById(teamId)) {
            return ResponseEntity.notFound().build();
        }
        List<ActivePerk> activePerks = perkActivationService.getActivePerksForTeam(teamId);
        return ResponseEntity.ok(activePerks);
    }
}
