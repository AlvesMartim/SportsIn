package org.SportsIn.services;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class InfluenceCalculator {

    private final List<InfluenceModifier> modifiers;

    public InfluenceCalculator(List<InfluenceModifier> modifiers) {
        this.modifiers = modifiers.stream()
                .sorted(Comparator.comparingInt(InfluenceModifier::getOrder))
                .toList();
    }

    public double computeTotalModifier(Long teamId, Long pointId) {
        double modifier = 0.0;
        for (InfluenceModifier mod : modifiers) {
            modifier = mod.apply(teamId, pointId, modifier);
        }
        return modifier;
    }
}
