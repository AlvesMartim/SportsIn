package org.SportsIn.services;

import org.SportsIn.model.Arene;
import org.SportsIn.model.mission.Mission;
import org.SportsIn.model.mission.MissionType;
import org.SportsIn.model.user.Equipe;
import org.SportsIn.repository.AreneRepository;
import org.SportsIn.repository.EquipeRepository;
import org.SportsIn.repository.MissionRepository;
import org.SportsIn.weather.WeatherClient;
import org.SportsIn.weather.WeatherForecastEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherFlashMissionServiceTest {

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private EquipeRepository equipeRepository;

    @Mock
    private AreneRepository areneRepository;

    @Mock
    private WeatherClient weatherClient;

    private WeatherFlashMissionService weatherFlashMissionService;

    @BeforeEach
    void setUp() {
        weatherFlashMissionService = new WeatherFlashMissionService(
                missionRepository,
                equipeRepository,
                areneRepository,
                weatherClient
        );
    }

    @Test
    void generateFlashMissionsForAllTeams_createsMissionForUpcomingExtremeEvent() {
        Equipe team = new Equipe("Alpha");
        team.setId(1L);

        Arene arena = new Arene("arena1", "Arena 1", 48.0, 2.0);

        WeatherForecastEntry stormSoon = new WeatherForecastEntry(
                Instant.now().plusSeconds(6 * 3600L),
                19.0,
                12.0,
                11.0,
                202,
                "Thunderstorm",
                "thunderstorm"
        );

        when(equipeRepository.findAll()).thenReturn(List.of(team));
        when(areneRepository.findAll()).thenReturn(List.of(arena));
        when(weatherClient.getForecast(anyDouble(), anyDouble(), eq(24))).thenReturn(List.of(stormSoon));
        when(missionRepository.countActiveByTeam(1L)).thenReturn(0L);
        when(missionRepository.findActiveByTeam(1L)).thenReturn(List.of());
        when(missionRepository.save(any(Mission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int created = weatherFlashMissionService.generateFlashMissionsForAllTeams();

        assertEquals(1, created);

        ArgumentCaptor<Mission> captor = ArgumentCaptor.forClass(Mission.class);
        verify(missionRepository).save(captor.capture());

        Mission mission = captor.getValue();
        assertEquals(MissionType.DIVERSITY_SPORT, mission.getType());
        assertTrue(mission.getPayloadJson().contains("WEATHER_FLASH"));
        assertTrue(mission.getTitle().contains("Alerte"));
    }

    @Test
    void generateFlashMissionsForAllTeams_returnsZero_whenNoExtremeEvent() {
        Arene arena = new Arene("arena1", "Arena 1", 48.0, 2.0);

        WeatherForecastEntry calm = new WeatherForecastEntry(
                Instant.now().plusSeconds(6 * 3600L),
                20.0,
                3.0,
                0.0,
                800,
                "Clear",
                "clear sky"
        );

        when(areneRepository.findAll()).thenReturn(List.of(arena));
        when(weatherClient.getForecast(anyDouble(), anyDouble(), eq(24))).thenReturn(List.of(calm));

        int created = weatherFlashMissionService.generateFlashMissionsForAllTeams();

        assertEquals(0, created);
    }
}
