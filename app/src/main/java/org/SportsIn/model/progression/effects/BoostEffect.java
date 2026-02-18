package org.SportsIn.model.progression.effects;

import org.SportsIn.model.progression.ActivePerk;
import org.SportsIn.model.progression.LevelThreshold;
import org.SportsIn.model.progression.PerkContext;
import org.SportsIn.model.progression.PerkDefinition;
import org.SportsIn.model.user.Equipe;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class BoostEffect implements PerkEffectStrategy {

    @Override
    public String getEffectType() {
        return "INFLUENCE_BOOST";
    }

    @Override
    public double computeInfluenceModifier(PerkContext context) {
        Object raw = context.parameters().get("boostPercent");
        double boost = raw instanceof Number n ? n.doubleValue() : 25.0;
        return context.baseInfluence() * boost / 100.0;
    }

    @Override
    public boolean canActivate(Equipe team, PerkDefinition def, List<ActivePerk> existingPerks) {
        int level = LevelThreshold.levelForXp(team.getXp());
        if (level < def.getRequiredLevel()) return false;

        long activeCount = existingPerks.stream()
                .filter(ap -> ap.getPerkDefinitionId().equals(def.getId()))
                .filter(ActivePerk::isActive)
                .count();
        if (activeCount >= def.getMaxActiveInstances()) return false;

        Optional<Instant> lastExpiry = existingPerks.stream()
                .filter(ap -> ap.getPerkDefinitionId().equals(def.getId()))
                .map(ActivePerk::getExpiresAtInstant)
                .max(Instant::compareTo);

        if (lastExpiry.isPresent()) {
            Instant cooldownEnd = lastExpiry.get().plusSeconds(def.getCooldownSeconds());
            if (Instant.now().isBefore(cooldownEnd)) return false;
        }

        return true;
    }
}
