package org.SportsIn.integration;

import org.SportsIn.model.Arene;
import org.SportsIn.model.MetricType;
import org.SportsIn.model.MetricValue;
import org.SportsIn.model.Participant;
import org.SportsIn.model.ParticipantType;
import org.SportsIn.model.Session;
import org.SportsIn.model.SessionRepository;
import org.SportsIn.model.SessionState;
import org.SportsIn.model.Sport;
import org.SportsIn.repository.AreneRepository;
import org.SportsIn.repository.SportRepository;
import org.SportsIn.services.SessionService;
import org.SportsIn.weather.WeatherClient;
import org.SportsIn.weather.WeatherSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "mission.scheduler.enabled=false",
        "weather.scheduler.enabled=false",
        "weather.openweather.enabled=false",
        "spring.datasource.url=jdbc:sqlite:./build/feature7_session_it.db"
})
class SessionWeatherFlowIntegrationTest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private AreneRepository areneRepository;

    @MockBean
    private WeatherClient weatherClient;

    @BeforeEach
    void resetArenaState() {
        Arene arena = areneRepository.findById("velodrome").orElseThrow();
        arena.setControllingTeam(null);
        areneRepository.save(arena);
    }

    @Test
    void processSessionCompletion_persistsWeatherMetadataAndUpdatesControl() {
        Sport football = sportRepository.findByCode("FOOTBALL").orElseThrow();

        Session session = new Session(
                "IT_SESSION_WEATHER_1",
                football,
                "velodrome",
                SessionState.ACTIVE,
                LocalDateTime.now().minusMinutes(10),
                List.of(
                        new Participant("1", "AS Monaco", ParticipantType.TEAM),
                        new Participant("2", "Paris Saint-Germain", ParticipantType.TEAM)
                )
        );
        session.getResult().setMetrics(List.of(
                new MetricValue("1", MetricType.GOALS, 2.0, "match"),
                new MetricValue("2", MetricType.GOALS, 0.0, "match")
        ));
        sessionRepository.save(session);

        when(weatherClient.getCurrentWeather(anyDouble(), anyDouble())).thenReturn(
                Optional.of(new WeatherSnapshot(12.0, 9.0, 7.0, 500, "Rain", "light rain", Instant.now()))
        );

        sessionService.processSessionCompletion("IT_SESSION_WEATHER_1");

        Session updated = sessionRepository.findById("IT_SESSION_WEATHER_1").orElseThrow();
        assertEquals(SessionState.TERMINATED, updated.getState());
        assertEquals("1", updated.getWinnerParticipantId());
        assertNotNull(updated.getResult());
        assertNotNull(updated.getResult().getWeatherHardshipIndex());
        assertNotNull(updated.getResult().getWeatherSummary());
        assertEquals("OPENWEATHER_CURRENT", updated.getResult().getWeatherSource());
        assertTrue(updated.getResult().getWeatherTags().contains("RAIN"));

        Arene captured = areneRepository.findById("velodrome").orElseThrow();
        assertEquals(1L, captured.getControllingTeamId());
    }
}
