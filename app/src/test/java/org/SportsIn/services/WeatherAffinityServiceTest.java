package org.SportsIn.services;

import org.SportsIn.model.progression.ActivePerk;
import org.SportsIn.model.progression.PerkDefinition;
import org.SportsIn.repository.ActivePerkRepository;
import org.SportsIn.repository.PerkDefinitionRepository;
import org.SportsIn.weather.SessionWeatherImpact;
import org.SportsIn.weather.WeatherConditionTag;
import org.SportsIn.weather.WeatherSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherAffinityServiceTest {

    @Mock
    private ActivePerkRepository activePerkRepository;

    @Mock
    private PerkDefinitionRepository perkDefinitionRepository;

    private WeatherAffinityService weatherAffinityService;

    @BeforeEach
    void setUp() {
        weatherAffinityService = new WeatherAffinityService(activePerkRepository, perkDefinitionRepository);
    }

    @Test
    void computeAffinityBonus_appliesWeatherAndFlatBoost_whenConditionMatches() {
        ActivePerk activePerk = new ActivePerk();
        activePerk.setPerkDefinitionId(10L);

        PerkDefinition definition = new PerkDefinition();
        definition.setId(10L);
        definition.setEffectType("WEATHER_AFFINITY");
        definition.setParametersJson("{\"condition\":\"RAIN\",\"weatherBonusBoostPercent\":100,\"flatInfluenceBonusPercent\":15}");

        when(activePerkRepository.findActiveByTeam(eq(1L), anyString())).thenReturn(java.util.List.of(activePerk));
        when(perkDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition));

        SessionWeatherImpact impact = new SessionWeatherImpact(
                new WeatherSnapshot(12.0, 6.0, 8.0, 500, "Rain", "rain", Instant.now()),
                1.40,
                0.40,
                Set.of(WeatherConditionTag.RAIN, WeatherConditionTag.EXTREME),
                "TEST"
        );

        double bonus = weatherAffinityService.computeAffinityBonus(1L, impact);
        // 100% of weather bonus (0.40) + flat 15% (0.15).
        assertEquals(0.55, bonus, 0.001);
    }

    @Test
    void computeAffinityBonus_returnsZero_whenConditionDoesNotMatch() {
        ActivePerk activePerk = new ActivePerk();
        activePerk.setPerkDefinitionId(11L);

        PerkDefinition definition = new PerkDefinition();
        definition.setId(11L);
        definition.setEffectType("WEATHER_AFFINITY");
        definition.setParametersJson("{\"condition\":\"HEAT\",\"weatherBonusBoostPercent\":50,\"flatInfluenceBonusPercent\":5}");

        when(activePerkRepository.findActiveByTeam(eq(2L), anyString())).thenReturn(java.util.List.of(activePerk));
        when(perkDefinitionRepository.findById(11L)).thenReturn(Optional.of(definition));

        SessionWeatherImpact impact = new SessionWeatherImpact(
                new WeatherSnapshot(8.0, 4.0, 0.0, 800, "Clear", "clear", Instant.now()),
            1.20,
            0.20,
            Set.of(WeatherConditionTag.RAIN),
                "TEST"
        );

        double bonus = weatherAffinityService.computeAffinityBonus(2L, impact);
        assertEquals(0.0, bonus, 0.001);
    }
}
