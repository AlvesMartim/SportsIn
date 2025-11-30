package org.SportsIn.services;

import org.SportsIn.model.*;
import org.SportsIn.model.territory.InMemoryPointSportifRepository;
import org.SportsIn.model.territory.PointSportif;
import org.SportsIn.model.territory.PointSportifRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
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
        // Initialisation des repositories en mémoire pour chaque test
        sessionRepository = new InMemorySessionRepository();
        pointSportifRepository = new InMemoryPointSportifRepository();
        
        // Injection des dépendances dans le service
        sessionService = new SessionService(sessionRepository, pointSportifRepository);

        // Création des données de test communes
        football = new Sport(1L, "FOOT", "Football", 101L, null);
        cityStade = new PointSportif(42L, "City Stade de la Villette", 48.89, 2.38, List.of(football));
        equipeA = new Participant("10", "Les Aigles", ParticipantType.TEAM);
        equipeB = new Participant("12", "Les Requins", ParticipantType.TEAM);

        // On sauvegarde le point dans son repository pour qu'il soit trouvable
        pointSportifRepository.save(cityStade);
    }

    @Test
    void testProcessSessionCompletion_SuccessfulConquest() {
        // ARRANGE : Créer une session où l'équipe A va gagner
        Session session = new Session("S_001", football, "42", SessionState.ACTIVE, LocalDateTime.now(), List.of(equipeA, equipeB));
        session.getResult().setMetrics(List.of(
            new MetricValue(equipeA.getId(), MetricType.GOALS, 3.0, "match"),
            new MetricValue(equipeB.getId(), MetricType.GOALS, 1.0, "match")
        ));
        sessionRepository.save(session);

        // ACT : Lancer le traitement du service
        sessionService.processSessionCompletion("S_001");

        // ASSERT : Vérifier les résultats
        PointSportif pointVerif = pointSportifRepository.findById(42L).orElseThrow();
        assertEquals(10L, pointVerif.getControllingTeamId(), "L'équipe A (ID 10) devrait contrôler le point.");

        Session sessionVerif = sessionRepository.findById("S_001").orElseThrow();
        assertEquals(SessionState.TERMINATED, sessionVerif.getState(), "La session devrait être marquée comme terminée.");
        assertEquals("10", sessionVerif.getWinnerParticipantId(), "L'ID du gagnant de la session devrait être celui de l'équipe A.");
    }

    @Test
    void testProcessSessionCompletion_NoWinner_ControlDoesNotChange() {
        // ARRANGE : Le point est initialement contrôlé par l'équipe B
        cityStade.setControllingTeamId(12L);
        pointSportifRepository.save(cityStade);

        // Créer une session avec un score d'égalité
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
        assertNull(sessionVerif.getWinnerParticipantId(), "Il ne devrait pas y avoir de gagnant pour la session.");
    }

    @Test
    void testProcessSessionCompletion_InvalidSessionId_ThrowsException() {
        // ARRANGE, ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> {
            sessionService.processSessionCompletion("ID_INEXISTANT");
        }, "Le service aurait dû lancer une exception pour un ID de session invalide.");
    }

    @Test
    void testProcessSessionCompletion_WithInvalidPointId_CompletesSessionWithoutCrashing() {
        // ARRANGE : Créer une session sur un point qui n'existe pas
        Session session = new Session("S_003", football, "9999", SessionState.ACTIVE, LocalDateTime.now(), List.of(equipeA, equipeB));
        session.getResult().setMetrics(List.of(
            new MetricValue(equipeA.getId(), MetricType.GOALS, 5.0, "match")
        ));
        sessionRepository.save(session);

        // ACT : Lancer le service
        // On s'attend à ce que le service ne crashe pas, même si le point n'est pas trouvé.
        sessionService.processSessionCompletion("S_003");

        // ASSERT
        Session sessionVerif = sessionRepository.findById("S_003").orElseThrow();
        assertEquals(SessionState.TERMINATED, sessionVerif.getState(), "La session doit être terminée même si le point est invalide.");
        assertEquals("10", sessionVerif.getWinnerParticipantId(), "Le gagnant de la session doit être correctement enregistré.");
    }
}
