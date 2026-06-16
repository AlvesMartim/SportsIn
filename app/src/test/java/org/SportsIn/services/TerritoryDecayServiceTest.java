package org.SportsIn.services;

import org.SportsIn.model.Arene;
import org.SportsIn.model.user.Equipe;
import org.SportsIn.repository.AreneRepository;
import org.SportsIn.weather.WeatherClient;
import org.SportsIn.weather.WeatherForecastEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TerritoryDecayServiceTest {

    @Mock
    private AreneRepository areneRepository;

    @Mock
    private WeatherClient weatherClient;

    private TerritoryInfluenceStateService influenceStateService;
    private TerritoryDecayService territoryDecayService;

    @BeforeEach
    void setUp() {
        influenceStateService = new TerritoryInfluenceStateService();
        territoryDecayService = new TerritoryDecayService(areneRepository, influenceStateService, weatherClient);
    }

    @Test
    void applyDailyDecay_removesControl_whenExtremeWeatherDepletesInfluence() {
        Equipe team = new Equipe("Team");
        team.setId(1L);

        Arene arena = new Arene("a1", "Arena", 48.0, 2.0);
        arena.setControllingTeam(team);

        when(areneRepository.findAll()).thenReturn(List.of(arena));
        when(areneRepository.save(any(Arene.class))).thenAnswer(invocation -> invocation.getArgument(0));

        influenceStateService.setLevel("a1", 6.0);
        when(weatherClient.getForecast(48.0, 2.0, 48)).thenReturn(extremeForecast());

        territoryDecayService.applyDailyDecay();

        assertNull(arena.getControllingTeam());
        verify(areneRepository).save(arena);
    }

    @Test
    void applyDailyDecay_appliesNaturalDecay_withoutNeutralizingArena() {
        Equipe team = new Equipe("Team");
        team.setId(1L);

        Arene arena = new Arene("a2", "Arena 2", 48.5, 2.5);
        arena.setControllingTeam(team);

        when(areneRepository.findAll()).thenReturn(List.of(arena));
        when(weatherClient.getForecast(48.5, 2.5, 48)).thenReturn(calmForecast());

        influenceStateService.setLevel("a2", 10.0);

        territoryDecayService.applyDailyDecay();

        assertEquals(7.0, influenceStateService.getInfluenceLevel("a2"), 0.001);
    }

    private List<WeatherForecastEntry> extremeForecast() {
        List<WeatherForecastEntry> entries = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            entries.add(new WeatherForecastEntry(
                    Instant.now().plusSeconds(i * 3L * 3600L),
                    37.0,
                    18.0,
                    12.0,
                    202,
                    "Thunderstorm",
                    "storm"
            ));
        }
        return entries;
    }

    private List<WeatherForecastEntry> calmForecast() {
        List<WeatherForecastEntry> entries = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            entries.add(new WeatherForecastEntry(
                    Instant.now().plusSeconds(i * 3L * 3600L),
                    19.0,
                    4.0,
                    0.0,
                    800,
                    "Clear",
                    "clear"
            ));
        }
        return entries;
    }
}
