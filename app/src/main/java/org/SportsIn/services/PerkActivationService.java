package org.SportsIn.services;

import org.SportsIn.model.progression.ActivePerk;
import org.SportsIn.model.progression.PerkDefinition;
import org.SportsIn.model.progression.effects.PerkEffectRegistry;
import org.SportsIn.model.progression.effects.PerkEffectStrategy;
import org.SportsIn.model.user.Equipe;
import org.SportsIn.repository.ActivePerkRepository;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.repository.PerkDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PerkActivationService {

    private final ActivePerkRepository activePerkRepository;
    private final PerkDefinitionRepository perkDefinitionRepository;
    private final EquipeRepository equipeRepository;
    private final PerkEffectRegistry perkEffectRegistry;

    public PerkActivationService(ActivePerkRepository activePerkRepository,
                                 PerkDefinitionRepository perkDefinitionRepository,
                                 EquipeRepository equipeRepository,
                                 PerkEffectRegistry perkEffectRegistry) {
        this.activePerkRepository = activePerkRepository;
        this.perkDefinitionRepository = perkDefinitionRepository;
        this.equipeRepository = equipeRepository;
        this.perkEffectRegistry = perkEffectRegistry;
    }

    @Transactional
    public ActivePerk activatePerk(Long teamId, String perkCode, String targetId) {
        PerkDefinition def = perkDefinitionRepository.findByCode(perkCode)
                .orElseThrow(() -> new IllegalArgumentException("Perk inconnu: " + perkCode));

        Equipe team = equipeRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Equipe inconnue: " + teamId));

        List<ActivePerk> existingPerks = activePerkRepository.findByTeamId(teamId);

        PerkEffectStrategy strategy = perkEffectRegistry.resolve(def.getEffectType());

        if (!strategy.canActivate(team, def, existingPerks)) {
            throw new IllegalStateException(
                    "Perk non activable: cooldown, niveau insuffisant, ou max atteint");
        }

        if (!def.isStackable()) {
            boolean alreadyOnTarget = existingPerks.stream()
                    .filter(ActivePerk::isActive)
                    .filter(ap -> ap.getPerkDefinitionId().equals(def.getId()))
                    .anyMatch(ap -> targetId != null && targetId.equals(ap.getTargetId()));
            if (alreadyOnTarget) {
                throw new IllegalStateException(
                        "Perk non stackable deja actif sur cette cible");
            }
        }

        Instant now = Instant.now();
        ActivePerk perk = new ActivePerk();
        perk.setTeamId(teamId);
        perk.setPerkDefinitionId(def.getId());
        perk.setTargetId(targetId);
        perk.setActivatedAt(now.toString());
        perk.setExpiresAt(now.plusSeconds(def.getDurationSeconds()).toString());
        perk.setUsageCount(1);

        return activePerkRepository.save(perk);
    }

    @Transactional
    public void deactivateExpiredPerks() {
        String now = Instant.now().toString();
        List<ActivePerk> expired = activePerkRepository.findExpiredBefore(now);
        if (!expired.isEmpty()) {
            activePerkRepository.deleteAll(expired);
        }
    }

    public List<ActivePerk> getActivePerksForTeam(Long teamId) {
        String now = Instant.now().toString();
        return activePerkRepository.findActiveByTeam(teamId, now);
    }

    public List<ActivePerk> getActivePerksOnTarget(String targetId) {
        String now = Instant.now().toString();
        return activePerkRepository.findActiveOnTarget(targetId, now);
    }
}
