package org.SportsIn.services;

import org.SportsIn.model.progression.ActivePerk;
import org.SportsIn.model.progression.PerkDefinition;
import org.SportsIn.repository.ActivePerkRepository;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.repository.PerkDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class XpGrantService {

    private static final int XP_MATCH_WIN = 30;
    private static final int XP_MATCH_LOSS = 10;
    private static final int XP_ROUTE_CONTROL = 15;
    private static final int XP_POINT_HOLDING_PER_HOUR = 5;

    private final EquipeRepository equipeRepository;
    private final ActivePerkRepository activePerkRepository;
    private final PerkDefinitionRepository perkDefinitionRepository;

    public XpGrantService(EquipeRepository equipeRepository,
                          ActivePerkRepository activePerkRepository,
                          PerkDefinitionRepository perkDefinitionRepository) {
        this.equipeRepository = equipeRepository;
        this.activePerkRepository = activePerkRepository;
        this.perkDefinitionRepository = perkDefinitionRepository;
    }

    @Transactional
    public void grantMatchXp(Long teamId, boolean won) {
        int base = won ? XP_MATCH_WIN : XP_MATCH_LOSS;
        int amount = applyXpMultiplier(teamId, base);
        addXp(teamId, amount);
    }

    @Transactional
    public void grantRouteControlXp(Long teamId, int routeCount) {
        int base = routeCount * XP_ROUTE_CONTROL;
        int amount = applyXpMultiplier(teamId, base);
        addXp(teamId, amount);
    }

    @Transactional
    public void grantPointHoldingXp(Long teamId, long holdingHours) {
        int base = (int) (holdingHours * XP_POINT_HOLDING_PER_HOUR);
        int amount = applyXpMultiplier(teamId, base);
        addXp(teamId, amount);
    }

    private int applyXpMultiplier(Long teamId, int baseXp) {
        if (activePerkRepository == null) return baseXp;
        String now = Instant.now().toString();
        List<ActivePerk> activePerks = activePerkRepository.findActiveByTeam(teamId, now);

        double multiplier = 1.0;
        for (ActivePerk ap : activePerks) {
            PerkDefinition def = perkDefinitionRepository.findById(ap.getPerkDefinitionId())
                    .orElse(null);
            if (def != null && "XP_MULTIPLIER".equals(def.getEffectType())) {
                var params = def.getParametersAsMap();
                Object raw = params.get("multiplier");
                double bonus = raw instanceof Number n ? n.doubleValue() : 1.5;
                multiplier *= bonus;
            }
        }

        return (int) Math.round(baseXp * multiplier);
    }

    private void addXp(Long teamId, int amount) {
        if (amount <= 0 || equipeRepository == null) return;
        equipeRepository.findById(teamId).ifPresent(equipe -> {
            equipe.setXp(equipe.getXp() + amount);
            equipeRepository.save(equipe);
        });
    }
}
