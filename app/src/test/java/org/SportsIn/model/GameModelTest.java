package org.SportsIn.model;

import org.SportsIn.model.user.Equipe;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void defaultConstructor_setsDefaultState() {
        Game game = new Game();
        assertEquals(GameState.WAITING, game.getState());
        assertNotNull(game.getCreatedAt());
    }

    @Test
    void parameterizedConstructor_setsFields() {
        Sport sport = new Sport();
        sport.setCode("FOOT");
        Equipe equipe = new Equipe("Rouge");

        Game game = new Game("G1", sport, "arena1", equipe);
        assertEquals("G1", game.getId());
        assertEquals("FOOT", game.getSport().getCode());
        assertEquals("arena1", game.getPointId());
        assertEquals("Rouge", game.getCreatorTeam().getNom());
        assertEquals(GameState.WAITING, game.getState());
    }

    @Test
    void gettersAndSetters() {
        Game game = new Game();
        game.setId("G2");
        game.setPointId("p1");
        game.setSessionId("S1");
        game.setWinnerTeamId("T1");
        game.setState(GameState.COMPLETED);

        LocalDateTime now = LocalDateTime.now();
        game.setStartedAt(now);
        game.setCompletedAt(now);

        assertEquals("G2", game.getId());
        assertEquals("p1", game.getPointId());
        assertEquals("S1", game.getSessionId());
        assertEquals("T1", game.getWinnerTeamId());
        assertEquals(GameState.COMPLETED, game.getState());
        assertEquals(now, game.getStartedAt());
        assertEquals(now, game.getCompletedAt());
    }

    @Test
    void opponentTeam_getterSetter() {
        Game game = new Game();
        assertNull(game.getOpponentTeam());

        Equipe opponent = new Equipe("Bleu");
        game.setOpponentTeam(opponent);
        assertEquals("Bleu", game.getOpponentTeam().getNom());
    }

    @Test
    void toString_containsId() {
        Game game = new Game("G1", null, "arena1", null);
        String str = game.toString();
        assertTrue(str.contains("G1"));
    }
}
