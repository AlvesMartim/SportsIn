package org.SportsIn.services;

import org.SportsIn.model.progression.ActivePerk;
import org.SportsIn.model.progression.PerkDefinition;
import org.SportsIn.repository.ActivePerkRepository;
import org.SportsIn.repository.PerkDefinitionRepository;
import org.SportsIn.weather.SessionWeatherImpact;
import org.SportsIn.weather.WeatherConditionTag;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Service
public class WeatherAffinityService {

    private final ActivePerkRepository activePerkRepository;
    private final PerkDefinitionRepository perkDefinitionRepository;

    public WeatherAffinityService(ActivePerkRepository activePerkRepository,
                                  PerkDefinitionRepository perkDefinitionRepository) {
        this.activePerkRepository = activePerkRepository;
        this.perkDefinitionRepository = perkDefinitionRepository;
    }

    public double computeAffinityBonus(Long teamId, SessionWeatherImpact weatherImpact) {
        if (teamId == null || weatherImpact == null || weatherImpact.tags().isEmpty()) {
            return 0.0;
        }

        String now = Instant.now().toString();
        double totalBonus = 0.0;

        for (ActivePerk activePerk : activePerkRepository.findActiveByTeam(teamId, now)) {
            PerkDefinition definition = perkDefinitionRepository.findById(activePerk.getPerkDefinitionId())
                    .orElse(null);
            if (definition == null || !"WEATHER_AFFINITY".equals(definition.getEffectType())) {
                continue;
            }

            Map<String, Object> params = definition.getParametersAsMap();
            String condition = toStringValue(params.get("condition"), "ANY_EXTREME");
            if (!matchesCondition(condition, weatherImpact)) {
                continue;
            }

            double weatherBoostPercent = toNumber(params.get("weatherBonusBoostPercent"), 0.0);
            double flatInfluenceBonusPercent = toNumber(params.get("flatInfluenceBonusPercent"), 0.0);

            totalBonus += weatherImpact.weatherInfluenceBonus() * (weatherBoostPercent / 100.0);
            totalBonus += flatInfluenceBonusPercent / 100.0;
        }

        return totalBonus;
    }

    private boolean matchesCondition(String condition, SessionWeatherImpact weatherImpact) {
        String normalized = condition.toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "RAIN" -> weatherImpact.tags().contains(WeatherConditionTag.RAIN);
            case "HEAT" -> weatherImpact.tags().contains(WeatherConditionTag.HEAT);
            case "COLD" -> weatherImpact.tags().contains(WeatherConditionTag.COLD);
            case "WIND" -> weatherImpact.tags().contains(WeatherConditionTag.WIND);
            case "STORM" -> weatherImpact.tags().contains(WeatherConditionTag.STORM);
            case "ANY_EXTREME" -> weatherImpact.tags().contains(WeatherConditionTag.EXTREME);
            default -> false;
        };
    }

    private double toNumber(Object raw, double fallback) {
        return raw instanceof Number number ? number.doubleValue() : fallback;
    }

    private String toStringValue(Object raw, String fallback) {
        if (raw instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
