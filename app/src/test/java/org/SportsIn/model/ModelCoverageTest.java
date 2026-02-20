package org.SportsIn.model;

import org.SportsIn.model.user.Equipe;
import org.SportsIn.model.user.Joueur;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelCoverageTest {

    // ---- EvaluationResult ----

    @Test
    void evaluationResult_getters() {
        EvaluationResult result = new EvaluationResult("P1", "Team P1 wins");
        assertEquals("P1", result.getWinnerParticipantId());
        assertEquals("Team P1 wins", result.getMessage());
    }

    @Test
    void evaluationResult_nullWinner() {
        EvaluationResult result = new EvaluationResult(null, "Draw");
        assertNull(result.getWinnerParticipantId());
        assertEquals("Draw", result.getMessage());
    }

    @Test
    void evaluationResult_toString() {
        EvaluationResult result = new EvaluationResult("P1", "Win");
        String str = result.toString();
        assertTrue(str.contains("P1"));
        assertTrue(str.contains("Win"));
    }

    // ---- Sport ----

    @Test
    void sport_defaultConstructor() {
        Sport sport = new Sport();
        assertNull(sport.getCode());
        assertNull(sport.getName());
    }

    @Test
    void sport_fullConstructor() {
        Sport sport = new Sport(1L, "FOOTBALL", "Football", 101L, 201L);
        assertEquals(1L, sport.getId());
        assertEquals("FOOTBALL", sport.getCode());
        assertEquals("Football", sport.getName());
        assertEquals(101L, sport.getVictoryRuleId());
        assertEquals(201L, sport.getScoringRuleId());
    }

    @Test
    void sport_gettersSetters() {
        Sport sport = new Sport();
        sport.setId(2L);
        sport.setCode("BASKET");
        sport.setName("Basket");
        sport.setVictoryRuleId(102L);
        sport.setScoringRuleId(202L);

        assertEquals(2L, sport.getId());
        assertEquals("BASKET", sport.getCode());
        assertEquals("Basket", sport.getName());
        assertEquals(102L, sport.getVictoryRuleId());
        assertEquals(202L, sport.getScoringRuleId());
    }

    @Test
    void sport_toString() {
        Sport sport = new Sport(1L, "FOOT", "Football", null, null);
        String str = sport.toString();
        assertTrue(str.contains("FOOT"));
    }

    // ---- MetricValue ----

    @Test
    void metricValue_defaultConstructor() {
        MetricValue mv = new MetricValue();
        assertNull(mv.getParticipantId());
    }

    @Test
    void metricValue_fullConstructor() {
        MetricValue mv = new MetricValue("P1", MetricType.GOALS, 3.0, "match1");
        assertEquals("P1", mv.getParticipantId());
        assertEquals(MetricType.GOALS, mv.getMetricType());
        assertEquals(3.0, mv.getValue(), 0.001);
        assertEquals("match1", mv.getContext());
    }

    @Test
    void metricValue_gettersSetters() {
        MetricValue mv = new MetricValue();
        mv.setParticipantId("P2");
        mv.setMetricType(MetricType.TIME_SECONDS);
        mv.setValue(120.5);
        mv.setContext("race");

        assertEquals("P2", mv.getParticipantId());
        assertEquals(MetricType.TIME_SECONDS, mv.getMetricType());
        assertEquals(120.5, mv.getValue(), 0.001);
        assertEquals("race", mv.getContext());
    }

    @Test
    void metricValue_toString() {
        MetricValue mv = new MetricValue("P1", MetricType.REPS, 10, "squat");
        assertTrue(mv.toString().contains("P1"));
    }

    // ---- Participant ----

    @Test
    void participant_defaultConstructor() {
        Participant p = new Participant();
        assertNull(p.getId());
    }

    @Test
    void participant_fullConstructor() {
        Participant p = new Participant("1", "Rouge", ParticipantType.TEAM);
        assertEquals("1", p.getId());
        assertEquals("Rouge", p.getName());
        assertEquals(ParticipantType.TEAM, p.getType());
    }

    @Test
    void participant_gettersSetters() {
        Participant p = new Participant();
        p.setId("2");
        p.setName("Bleu");
        p.setType(ParticipantType.PLAYER);

        assertEquals("2", p.getId());
        assertEquals("Bleu", p.getName());
        assertEquals(ParticipantType.PLAYER, p.getType());
    }

    @Test
    void participant_toString() {
        Participant p = new Participant("1", "Rouge", ParticipantType.TEAM);
        assertTrue(p.toString().contains("Rouge"));
    }

    // ---- SessionResult ----

    @Test
    void sessionResult_defaultConstructor() {
        SessionResult sr = new SessionResult();
        assertNull(sr.getMetrics());
    }

    @Test
    void sessionResult_withSessionAndMetrics() {
        Session session = new Session();
        session.setId("S1");
        java.util.List<MetricValue> metrics = java.util.List.of(
                new MetricValue("P1", MetricType.GOALS, 2, "match"));

        SessionResult sr = new SessionResult(session, metrics);
        assertEquals(session, sr.getSession());
        assertEquals(1, sr.getMetrics().size());
    }

    @Test
    void sessionResult_gettersSetters() {
        SessionResult sr = new SessionResult();
        sr.setSession(new Session());
        sr.setMetrics(java.util.List.of());
        assertNotNull(sr.getSession());
        assertTrue(sr.getMetrics().isEmpty());
    }

    // ---- Arene ----

    @Test
    void arene_defaultConstructor() {
        Arene arene = new Arene();
        assertNull(arene.getId());
    }

    @Test
    void arene_fullConstructor() {
        Arene arene = new Arene("A1", "Parc des Princes", 48.84, 2.25);
        assertEquals("A1", arene.getId());
        assertEquals("Parc des Princes", arene.getNom());
        assertEquals(48.84, arene.getLatitude(), 0.001);
        assertEquals(2.25, arene.getLongitude(), 0.001);
    }

    @Test
    void arene_controllingTeam() {
        Arene arene = new Arene("A1", "Arena", 0, 0);
        assertNull(arene.getControllingTeamId());

        Equipe equipe = new Equipe("Rouge");
        equipe.setId(5L);
        arene.setControllingTeam(equipe);
        assertEquals(5L, arene.getControllingTeamId());
        assertEquals(equipe, arene.getControllingTeam());
    }

    @Test
    void arene_sportsDisponibles() {
        Arene arene = new Arene();
        arene.setSportsDisponibles(java.util.List.of("FOOT", "BASKET"));
        assertEquals(2, arene.getSportsDisponibles().size());
    }

    @Test
    void arene_toString() {
        Arene arene = new Arene("A1", "Arena", 48.0, 2.0);
        String str = arene.toString();
        assertTrue(str.contains("A1"));
        assertTrue(str.contains("Arena"));
    }

    // ---- Equipe ----

    @Test
    void equipe_defaultConstructor() {
        Equipe equipe = new Equipe();
        assertNull(equipe.getId());
        assertEquals(0, equipe.getPoints());
        assertEquals(0, equipe.getXp());
    }

    @Test
    void equipe_nomConstructor() {
        Equipe equipe = new Equipe("Alpha");
        assertEquals("Alpha", equipe.getNom());
    }

    @Test
    void equipe_nomCouleurConstructor() {
        Equipe equipe = new Equipe("Beta", "red");
        assertEquals("Beta", equipe.getNom());
        assertEquals("red", equipe.getCouleur());
    }

    @Test
    void equipe_gettersSetters() {
        Equipe equipe = new Equipe();
        equipe.setId(10L);
        equipe.setNom("Gamma");
        equipe.setPoints(100);
        equipe.setXp(500);
        equipe.setCouleur("blue");

        assertEquals(10L, equipe.getId());
        assertEquals("Gamma", equipe.getNom());
        assertEquals(100, equipe.getPoints());
        assertEquals(500, equipe.getXp());
        assertEquals("blue", equipe.getCouleur());
    }

    @Test
    void equipe_addRemoveJoueur() {
        Equipe equipe = new Equipe("Team");
        Joueur joueur = new Joueur("player1");

        equipe.addJoueur(joueur);
        assertEquals(1, equipe.getMembres().size());
        assertEquals(equipe, joueur.getEquipe());

        equipe.removeJoueur(joueur);
        assertEquals(0, equipe.getMembres().size());
        assertNull(joueur.getEquipe());
    }

    @Test
    void equipe_toString() {
        Equipe equipe = new Equipe("TestTeam");
        equipe.setId(1L);
        assertTrue(equipe.toString().contains("TestTeam"));
    }

    // ---- Joueur ----

    @Test
    void joueur_defaultConstructor() {
        Joueur joueur = new Joueur();
        assertNull(joueur.getPseudo());
    }

    @Test
    void joueur_pseudoConstructor() {
        Joueur joueur = new Joueur("player1");
        assertEquals("player1", joueur.getPseudo());
    }

    @Test
    void joueur_fullConstructor() {
        Joueur joueur = new Joueur("player1", "p1@test.com", "pass123");
        assertEquals("player1", joueur.getPseudo());
        assertEquals("p1@test.com", joueur.getEmail());
        assertEquals("pass123", joueur.getPassword());
    }

    @Test
    void joueur_gettersSetters() {
        Joueur joueur = new Joueur();
        joueur.setId(1L);
        joueur.setPseudo("p1");
        joueur.setEmail("p1@x.com");
        joueur.setPassword("secret");

        assertEquals(1L, joueur.getId());
        assertEquals("p1", joueur.getPseudo());
        assertEquals("p1@x.com", joueur.getEmail());
        assertEquals("secret", joueur.getPassword());
    }

    @Test
    void joueur_equipeRelation() {
        Joueur joueur = new Joueur("p1");
        Equipe equipe = new Equipe("Team");
        joueur.setEquipe(equipe);
        assertEquals(equipe, joueur.getEquipe());
    }

    @Test
    void joueur_toString() {
        Joueur joueur = new Joueur("TestPlayer");
        assertTrue(joueur.toString().contains("TestPlayer"));
    }

    // ---- Enums ----

    @Test
    void gameState_values() {
        assertEquals(4, GameState.values().length);
        assertNotNull(GameState.valueOf("WAITING"));
        assertNotNull(GameState.valueOf("MATCHED"));
        assertNotNull(GameState.valueOf("IN_PROGRESS"));
        assertNotNull(GameState.valueOf("COMPLETED"));
    }

    @Test
    void sessionState_values() {
        assertEquals(2, SessionState.values().length);
        assertNotNull(SessionState.valueOf("ACTIVE"));
        assertNotNull(SessionState.valueOf("TERMINATED"));
    }

    @Test
    void metricType_values() {
        assertEquals(5, MetricType.values().length);
        assertNotNull(MetricType.valueOf("GOALS"));
        assertNotNull(MetricType.valueOf("POINTS"));
        assertNotNull(MetricType.valueOf("TIME_SECONDS"));
        assertNotNull(MetricType.valueOf("REPS"));
        assertNotNull(MetricType.valueOf("CUSTOM"));
    }

    @Test
    void participantType_values() {
        assertEquals(2, ParticipantType.values().length);
        assertNotNull(ParticipantType.valueOf("PLAYER"));
        assertNotNull(ParticipantType.valueOf("TEAM"));
    }
}
