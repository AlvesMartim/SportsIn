package org.SportsIn.integration;

import org.SportsIn.model.mission.Mission;
import org.SportsIn.model.mission.MissionType;
import org.SportsIn.repository.MissionRepository;
import org.SportsIn.services.WeatherFlashMissionService;
import org.SportsIn.weather.WeatherClient;
import org.SportsIn.weather.WeatherForecastEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "mission.scheduler.enabled=false",
        "weather.scheduler.enabled=false",
        "weather.openweather.enabled=false",
        "spring.datasource.url=jdbc:sqlite:./build/feature7_flash_it.db"
})
class WeatherFlashMissionIntegrationTest {

    @Autowired
    private WeatherFlashMissionService weatherFlashMissionService;

    @Autowired
    private MissionRepository missionRepository;

    @MockBean
    private WeatherClient weatherClient;

    @BeforeEach
    void clearMissions() {
        missionRepository.deleteAll();
    }

    @Test
    void generateFlashMissionsForAllTeams_createsWeatherPayloadMissions() {
        WeatherForecastEntry extremeEvent = new WeatherForecastEntry(
                Instant.now().plusSeconds(6 * 3600L),
                36.0,
                13.0,
                11.0,
                202,
                "Thunderstorm",
                "storm incoming"
        );

        when(weatherClient.getForecast(anyDouble(), anyDouble(), eq(24))).thenReturn(List.of(extremeEvent));

        int created = weatherFlashMissionService.generateFlashMissionsForAllTeams();
        List<Mission> missions = missionRepository.findAll();

        assertTrue(created > 0);
        assertFalse(missions.isEmpty());
        assertTrue(missions.stream().allMatch(m -> m.getType() == MissionType.DIVERSITY_SPORT));
        assertTrue(missions.stream().anyMatch(m -> m.getPayloadJson() != null && m.getPayloadJson().contains("WEATHER_FLASH")));
    }
}
