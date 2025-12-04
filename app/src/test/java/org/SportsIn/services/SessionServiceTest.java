package org.SportsIn.services;

import org.SportsIn.model.*;
import org.SportsIn.model.territory.InMemoryPointSportifRepository;
import org.SportsIn.model.territory.PointSportif;
import org.SportsIn.model.territory.PointSportifRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    private SessionService sessionService;
    private SessionRepository sessionRepository;
    private PointSportifRepository pointSportifRepository;

    private Sport football;
    private PointSportif cityStade;
    private Participant equipeA;
    private Participant equipeB;

    @BeforeEach
    void setUp() {
        sessionRepository = new InMemorySessionRepository();
        pointSportifRepository = new InMemoryPointSportifRepository();
        sessionService = new SessionService(sessionRepository, pointSportifRepository);

        football = new Sport(1L, "FOOT", "Football", 101L, null);
        cityStade = new PointSportif(42L, "City Stade de la Villette", 48.89, 2.38, List.of(football));
        equipeA = new Participant("10", "Les Aigles", ParticipantType.TEAM);
        equipeB = new Participant("12", "Les Requins", ParticipantType.TEAM);

        pointSportifRepository.save(cityStade);
    }

    @Test
    @DisplayName("Cas nominal : une équipe conquiert un point neutre")
    void testProcessSessionCompletion_SuccessfulConquest() {
        // ARRANGE
        Session session = new Session("S_001", football, "42", SessionState.ACTIVE, LocalDateTime.now(), List.of(equipeA, equipeB));
        session.getResult().setMetrics(List.of(
            new MetricValue(equipeA.getId(), MetricType.GOALS, 3.0, "match"),
            new MetricValue(equipeB.getId(), MetricType.GOALS, 1.0, "match")
        ));
        sessionRepository.save(session);

        // ACT
        sessionService.processSessionCompletion("S_001");

        // ASSERT
        PointSportif pointVerif = pointSportifRepository.findById(42L).orElseThrow();
        assertEquals(10L, pointVerif.getControllingTeamId(), "L'équipe A (ID 10) devrait contrôler le point.");

        Session sessionVerif = sessionRepository.findById("S_001").orElseThrow();
        assertEquals(SessionState.TERMINATED, sessionVerif.getState());
        assertEquals("10", sessionVerif.getWinnerParticipantId());
    }

    @Test
    @DisplayName("Cas d'égalité : le contrôle du point ne change pas")
    void testProcessSessionCompletion_NoWinner_ControlDoesNotChange() {
        // ARRANGE
        cityStade.setControllingTeamId(12L);
        pointSportifRepository.save(cityStade);

        Session session = new Session("S_002", football, "42", SessionState.ACTIVE, LocalDateTime.now(), List.of(equipeA, equipeB));
        session.getResult().setMetrics(List.of(
            new MetricValue(equipeA.getId(), MetricType.GOALS, 2.0, "match"),
            new MetricValue(equipeB.getId(), MetricType.GOALS, 2.0, "match")
        ));
        sessionRepository.save(session);

        // ACT
        sessionService.processSessionCompletion("S_002");

        // ASSERT
        PointSportif pointVerif = pointSportifRepository.findById(42L).orElseThrow();
        assertEquals(12L, pointVerif.getControllingTeamId(), "Le contrôle du point ne devrait pas avoir changé.");

        Session sessionVerif = sessionRepository.findById("S_002").orElseThrow();
        assertEquals(SessionState.TERMINATED, sessionVerif.getState());
        assertNull(sessionVerif.getWinnerParticipantId());
    }
    
    @Test
    @DisplayName("Nouveau test : une équipe conquiert un point adverse")
    void testProcessSessionCompletion_ConquersOpponentPoint() {
        // ARRANGE : Le point est contrôlé par l'équipe B
        cityStade.setControllingTeamId(12L);
        pointSportifRepository.save(cityStade);

        Session session = new Session("S_004", football, "42", SessionState.ACTIVE, LocalDateTime.now(), List.of(equipeA, equipeB));
        session.getResult().setMetrics(List.of(
                new MetricValue(equipeA.getId(), MetricType.GOALS, 5.0, "match"),
                new MetricValue(equipeB.getId(), MetricType.GOALS, 0.0, "match")
        ));
        sessionRepository.save(session);

        // ACT
        sessionService.processSessionCompletion("S_004");

        // ASSERT
        PointSportif pointVerif = pointSportifRepository.findById(42L).orElseThrow();
        assertEquals(10L, pointVerif.getControllingTeamId(), "Le contrôle doit basculer de l'équipe B (12) à l'équipe A (10).");
    }

    @Test
    @DisplayName("Nouveau test : une équipe défend son propre point")
    void testProcessSessionCompletion_DefendsOwnPoint() {
        // ARRANGE : Le point est déjà contrôlé par l'équipe A
        cityStade.setControllingTeamId(10L);
        pointSportifRepository.save(cityStade);

        Session session = new Session("S_005", football, "42", SessionState.ACTIVE, LocalDateTime.now(), List.of(equipeA, equipeB));
        session.getResult().setMetrics(List.of(
                new MetricValue(equipeA.getId(), MetricType.GOALS, 2.0, "match"),
                new MetricValue(equipeB.getId(), MetricType.GOALS, 1.0, "match")
        ));
        sessionRepository.save(session);

        // ACT
        sessionService.processSessionCompletion("S_005");

        // ASSERT
        PointSportif pointVerif = pointSportifRepository.findById(42L).orElseThrow();
        assertEquals(10L, pointVerif.getControllingTeamId(), "Le contrôle doit rester à l'équipe A.");
    }

    @Test
    @DisplayName("Nouveau test : un seul joueur prend le contrôle d'un point")
    void testProcessSessionCompletion_SinglePlayerTakesControl() {
        // ARRANGE
        Session session = new Session("S_006", football, "42", SessionState.ACTIVE, LocalDateTime.now(), Collections.singletonList(equipeA));
        session.getResult().setMetrics(List.of(
                new MetricValue(equipeA.getId(), MetricType.GOALS, 10.0, "entrainement")
        ));
        sessionRepository.save(session);

        // ACT
        sessionService.processSessionCompletion("S_006");

        // ASSERT
        PointSportif pointVerif = pointSportifRepository.findById(42L).orElseThrow();
        assertEquals(10L, pointVerif.getControllingTeamId(), "Le joueur seul doit prendre le contrôle du point.");
    }

    @Test
    @DisplayName("Cas d'erreur : l'ID de session est invalide")
    void testProcessSessionCompletion_InvalidSessionId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            sessionService.processSessionCompletion("ID_INEXISTANT");
        });
    }

    @Test
    @DisplayName("Cas d'erreur : l'ID de point est invalide")
    void testProcessSessionCompletion_WithInvalidPointId_CompletesSessionWithoutCrashing() {
        // ARRANGE
        Session session = new Session("S_003", football, "9999", SessionState.ACTIVE, LocalDateTime.now(), List.of(equipeA, equipeB));
        session.getResult().setMetrics(List.of(new MetricValue(equipeA.getId(), MetricType.GOALS, 5.0, "match")));
        sessionRepository.save(session);

        // ACT & ASSERT
        // Le service ne doit pas planter
        assertDoesNotThrow(() -> sessionService.processSessionCompletion("S_003"));

        // La session doit quand même être terminée
        Session sessionVerif = sessionRepository.findById("S_003").orElseThrow();
        assertEquals(SessionState.TERMINATED, sessionVerif.getState());
        assertEquals("10", sessionVerif.getWinnerParticipantId());
    }
}
