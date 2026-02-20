package org.SportsIn.services;

import org.SportsIn.model.*;
import org.SportsIn.model.rules.FootballVictoryRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuleEvaluationServiceTest {

    private RuleEvaluationService service;
    private InMemoryRuleRepository ruleRepository;

    @BeforeEach
    void setUp() {
        ruleRepository = new InMemoryRuleRepository();
        service = new RuleEvaluationService(ruleRepository);
    }

    private Session makeSession(Sport sport, List<MetricValue> metrics) {
        Session session = new Session();
        session.setSport(sport);
        session.setPointId("arena1");
        session.setState(SessionState.ACTIVE);
        session.setCreatedAt(LocalDateTime.now());
        session.setParticipants(List.of(
                new Participant("1", "Rouge", ParticipantType.TEAM),
                new Participant("2", "Bleu", ParticipantType.TEAM)));
        SessionResult result = new SessionResult();
        result.setMetrics(metrics);
        session.setResult(result);
        return session;
    }

    @Test
    void evaluateVictory_nullSession() {
        assertNull(service.evaluateVictory(null));
    }

    @Test
    void evaluateVictory_nullSport() {
        Session session = new Session();
        session.setSport(null);
        assertNull(service.evaluateVictory(session));
    }

    @Test
    void evaluateVictory_withRegisteredRule() {
        Sport sport = new Sport();
        sport.setCode("FOOTBALL");
        sport.setVictoryRuleId(101L); // matches FootballVictoryRule in InMemoryRuleRepository

        List<MetricValue> metrics = List.of(
                new MetricValue("1", MetricType.GOALS, 3, "match"),
                new MetricValue("2", MetricType.GOALS, 1, "match"));

        Session session = makeSession(sport, metrics);
        EvaluationResult result = service.evaluateVictory(session);

        assertNotNull(result);
        assertEquals("1", result.getWinnerParticipantId());
    }

    @Test
    void evaluateVictory_fallbackWhenNoRuleId() {
        Sport sport = new Sport();
        sport.setCode("UNKNOWN");
        sport.setVictoryRuleId(null);

        List<MetricValue> metrics = List.of(
                new MetricValue("1", MetricType.GOALS, 2, "match"),
                new MetricValue("2", MetricType.GOALS, 5, "match"));

        Session session = makeSession(sport, metrics);
        EvaluationResult result = service.evaluateVictory(session);

        assertNotNull(result);
        assertEquals("2", result.getWinnerParticipantId());
    }

    @Test
    void evaluateVictory_unknownRuleIdUsesDefaultRule() {
        Sport sport = new Sport();
        sport.setCode("OTHER");
        sport.setVictoryRuleId(999L); // not registered, InMemoryRuleRepo returns fallback lambda

        List<MetricValue> metrics = List.of(
                new MetricValue("1", MetricType.GOALS, 1, "match"));

        Session session = makeSession(sport, metrics);
        EvaluationResult result = service.evaluateVictory(session);

        // The fallback rule returns null winner with "Aucune règle trouvée" message
        assertNotNull(result);
        assertNull(result.getWinnerParticipantId());
        assertTrue(result.getMessage().contains("Aucune règle"));
    }

    @Test
    void evaluateVictory_drawReturnsNullWinner() {
        Sport sport = new Sport();
        sport.setCode("FOOTBALL");
        sport.setVictoryRuleId(101L);

        List<MetricValue> metrics = List.of(
                new MetricValue("1", MetricType.GOALS, 2, "match"),
                new MetricValue("2", MetricType.GOALS, 2, "match"));

        Session session = makeSession(sport, metrics);
        EvaluationResult result = service.evaluateVictory(session);

        assertNotNull(result);
        assertNull(result.getWinnerParticipantId());
    }
}
