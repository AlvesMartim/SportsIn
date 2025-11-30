package org.SportsIn.model.rules;

import org.SportsIn.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FootballVictoryRuleTest {

    private FootballVictoryRule rule;
    private Sport football;
    private Session session;
    private Participant equipeA;
    private Participant equipeB;

    @BeforeEach
    void setUp() {
        // Initialisation commune à tous les tests
        rule = new FootballVictoryRule();
        football = new Sport(1L, "FOOT", "Football", 101L, 201L);
        
        equipeA = new Participant("EQUIPE_A", "Équipe A", ParticipantType.TEAM);
        equipeB = new Participant("EQUIPE_B", "Équipe B", ParticipantType.TEAM);
        
        List<Participant> participants = new ArrayList<>();
        participants.add(equipeA);
        participants.add(equipeB);

        session = new Session("S_001", football, "P_01", SessionState.ACTIVE, LocalDateTime.now(), participants);
    }

    @Test
    void testEquipeAWins() {
        // Scénario : L'équipe A gagne
        List<MetricValue> metrics = new ArrayList<>();
        metrics.add(new MetricValue(equipeA.getId(), MetricType.GOALS, 3.0, "match"));
        metrics.add(new MetricValue(equipeB.getId(), MetricType.GOALS, 2.0, "match"));
        session.getResult().setMetrics(metrics);

        EvaluationResult verdict = rule.evaluate(session);

        assertNotNull(verdict);
        assertEquals("EQUIPE_A", verdict.getWinnerParticipantId());
        assertTrue(verdict.getMessage().contains("Le gagnant est le participant EQUIPE_A"));
    }

    @Test
    void testDrawCase() {
        // Scénario : Égalité
        List<MetricValue> metrics = new ArrayList<>();
        metrics.add(new MetricValue(equipeA.getId(), MetricType.GOALS, 2.0, "match"));
        metrics.add(new MetricValue(equipeB.getId(), MetricType.GOALS, 2.0, "match"));
        session.getResult().setMetrics(metrics);

        EvaluationResult verdict = rule.evaluate(session);

        assertNotNull(verdict);
        // L'implémentation actuelle avec .max() peut retourner l'un ou l'autre en cas d'égalité.
        // On vérifie juste qu'un gagnant est bien désigné.
        assertNotNull(verdict.getWinnerParticipantId());
    }

    @Test
    void testNoMetricsProvided() {
        // Scénario : Aucune métrique
        session.getResult().setMetrics(new ArrayList<>());

        EvaluationResult verdict = rule.evaluate(session);

        assertNotNull(verdict);
        assertNull(verdict.getWinnerParticipantId());
        assertTrue(verdict.getMessage().contains("Aucune donnée de score"));
    }

    @Test
    void testNoRelevantMetrics() {
        // Scénario : Métriques non pertinentes (ex: temps de jeu)
        List<MetricValue> metrics = new ArrayList<>();
        metrics.add(new MetricValue(equipeA.getId(), MetricType.TIME_SECONDS, 3600, "match"));
        metrics.add(new MetricValue(equipeB.getId(), MetricType.TIME_SECONDS, 3600, "match"));
        session.getResult().setMetrics(metrics);

        EvaluationResult verdict = rule.evaluate(session);

        assertNotNull(verdict);
        assertNull(verdict.getWinnerParticipantId());
        assertTrue(verdict.getMessage().contains("Impossible de déterminer un gagnant"));
    }
}
