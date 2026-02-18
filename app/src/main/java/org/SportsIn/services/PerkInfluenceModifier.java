package org.SportsIn.services;

import org.SportsIn.model.progression.ActivePerk;
import org.SportsIn.model.progression.PerkContext;
import org.SportsIn.model.progression.PerkDefinition;
import org.SportsIn.model.progression.effects.PerkEffectRegistry;
import org.SportsIn.model.progression.effects.PerkEffectStrategy;
import org.SportsIn.repository.ActivePerkRepository;
import org.SportsIn.repository.PerkDefinitionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class PerkInfluenceModifier implements InfluenceModifier {

    private final ActivePerkRepository activePerkRepository;
    private final PerkDefinitionRepository perkDefinitionRepository;
    private final PerkEffectRegistry perkEffectRegistry;

    public PerkInfluenceModifier(ActivePerkRepository activePerkRepository,
                                 PerkDefinitionRepository perkDefinitionRepository,
                                 PerkEffectRegistry perkEffectRegistry) {
        this.activePerkRepository = activePerkRepository;
        this.perkDefinitionRepository = perkDefinitionRepository;
        this.perkEffectRegistry = perkEffectRegistry;
    }

    @Override
    public double apply(Long teamId, String pointId, double currentModifier) {
        String now = Instant.now().toString();
        List<ActivePerk> perksOnPoint = activePerkRepository.findActiveOnTarget(
                pointId, now);

        double perkModifier = 0.0;

        for (ActivePerk activePerk : perksOnPoint) {
            PerkDefinition def = perkDefinitionRepository.findById(activePerk.getPerkDefinitionId())
                    .orElse(null);
            if (def == null) continue;

            if (!perkEffectRegistry.hasEffect(def.getEffectType())) continue;

            PerkEffectStrategy strategy = perkEffectRegistry.resolve(def.getEffectType());

            PerkContext context = new PerkContext(
                    teamId,
                    activePerk.getTeamId(),
                    pointId,
                    currentModifier,
                    def.getParametersAsMap()
            );

            boolean isOwnPerk = activePerk.getTeamId().equals(teamId);
            boolean isBoost = "INFLUENCE_BOOST".equals(def.getEffectType());
            boolean isReduction = "INFLUENCE_REDUCTION".equals(def.getEffectType());

            // Le boost s'applique au proprietaire, le shield reduit l'influence adverse
            if ((isBoost && isOwnPerk) || (isReduction && !isOwnPerk)) {
                perkModifier += strategy.computeInfluenceModifier(context);
            }
        }

        return currentModifier + perkModifier;
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
