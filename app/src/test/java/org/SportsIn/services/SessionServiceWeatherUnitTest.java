package org.SportsIn.services;

import org.SportsIn.model.EvaluationResult;
import org.SportsIn.model.MetricType;
import org.SportsIn.model.MetricValue;
import org.SportsIn.model.Participant;
import org.SportsIn.model.ParticipantType;
import org.SportsIn.model.Session;
import org.SportsIn.model.SessionRepository;
import org.SportsIn.model.SessionResult;
import org.SportsIn.model.SessionState;
import org.SportsIn.model.Sport;
import org.SportsIn.weather.SessionWeatherImpact;
import org.SportsIn.weather.SessionWeatherService;
import org.SportsIn.weather.WeatherConditionTag;
import org.SportsIn.weather.WeatherSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceWeatherUnitTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private TerritoryService territoryService;

    @Mock
    private XpGrantService xpGrantService;

    @Mock
    private RuleEvaluationService ruleEvaluationService;

    @Mock
    private SessionWeatherService sessionWeatherService;

    @Mock
    private WeatherAffinityService weatherAffinityService;

    @Test
    void processSessionCompletion_appliesWeatherAndAffinityToInfluenceGain() {
        SessionService service = new SessionService(
                sessionRepository,
                territoryService,
                xpGrantService,
                ruleEvaluationService,
                sessionWeatherService,
                weatherAffinityService
        );

        Sport football = new Sport();
        football.setCode("FOOTBALL");

        Session session = new Session(
                "SESSION_WEATHER_1",
                football,
                "arena_weather",
                SessionState.ACTIVE,
                LocalDateTime.now().minusMinutes(20),
                List.of(
                        new Participant("10", "Team Red", ParticipantType.TEAM),
                        new Participant("12", "Team Blue", ParticipantType.TEAM)
                )
        );
        session.setResult(new SessionResult(session, List.of(
                new MetricValue("10", MetricType.GOALS, 3, "match"),
                new MetricValue("12", MetricType.GOALS, 1, "match")
        )));

        SessionWeatherImpact impact = new SessionWeatherImpact(
                new WeatherSnapshot(11.0, 10.0, 7.0, 500, "Rain", "moderate rain", Instant.now()),
                1.40,
                0.40,
                Set.of(WeatherConditionTag.RAIN, WeatherConditionTag.EXTREME),
                "TEST"
        );

        when(sessionRepository.findById("SESSION_WEATHER_1")).thenReturn(Optional.of(session));
        when(ruleEvaluationService.evaluateVictory(session)).thenReturn(new EvaluationResult("10", "winner"));
        when(sessionWeatherService.analyze(session)).thenReturn(impact);
        when(weatherAffinityService.computeAffinityBonus(10L, impact)).thenReturn(0.15);
        when(territoryService.getScoreBonusForTeamOnPoint(10L, "arena_weather")).thenReturn(0.10);
        doNothing().when(xpGrantService).grantMatchXp(eq(10L), anyBoolean());
        doNothing().when(xpGrantService).grantMatchXp(eq(12L), anyBoolean());

        service.processSessionCompletion("SESSION_WEATHER_1");

        ArgumentCaptor<Double> gainCaptor = ArgumentCaptor.forClass(Double.class);
        verify(territoryService).updateTerritoryControl(eq("arena_weather"), eq(10L), gainCaptor.capture());

        // Base 25 * (1 + route/perks 0.10 + weather 0.40 + affinity 0.15) = 41.25
        assertEquals(41.25, gainCaptor.getValue(), 0.001);
        assertEquals(SessionState.TERMINATED, session.getState());
        assertEquals("10", session.getWinnerParticipantId());
        assertNotNull(session.getEndedAt());

        SessionResult result = session.getResult();
        assertNotNull(result);
        assertEquals(1.40, result.getWeatherHardshipIndex(), 0.001);
        assertEquals(0.40, result.getWeatherInfluenceBonus(), 0.001);
        assertEquals(0.15, result.getWeatherAffinityBonus(), 0.001);
        assertEquals(0.65, result.getTotalInfluenceModifier(), 0.001);
        assertEquals("TEST", result.getWeatherSource());
        assertTrue(result.getWeatherTags().contains("RAIN"));
    }
}
