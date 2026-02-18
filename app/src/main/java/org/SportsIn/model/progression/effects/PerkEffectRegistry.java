package org.SportsIn.model.progression.effects;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PerkEffectRegistry {

    private final Map<String, PerkEffectStrategy> strategies;

    public PerkEffectRegistry(List<PerkEffectStrategy> allStrategies) {
        this.strategies = allStrategies.stream()
                .collect(Collectors.toMap(
                    PerkEffectStrategy::getEffectType,
                    Function.identity()
                ));
    }

    public PerkEffectStrategy resolve(String effectType) {
        PerkEffectStrategy strategy = strategies.get(effectType);
        if (strategy == null) {
            throw new IllegalArgumentException("Aucun effet connu pour le type: " + effectType);
        }
        return strategy;
    }

    public boolean hasEffect(String effectType) {
        return strategies.containsKey(effectType);
    }
}
