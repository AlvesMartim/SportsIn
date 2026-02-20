package org.SportsIn.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationResultTest {

    @Test
    void constructor_setsFields() {
        EvaluationResult result = new EvaluationResult("P1", "Team P1 wins");
        assertEquals("P1", result.getWinnerParticipantId());
        assertEquals("Team P1 wins", result.getMessage());
    }

    @Test
    void constructor_nullWinner() {
        EvaluationResult result = new EvaluationResult(null, "Draw");
        assertNull(result.getWinnerParticipantId());
        assertEquals("Draw", result.getMessage());
    }

    @Test
    void toString_containsInfo() {
        EvaluationResult result = new EvaluationResult("P1", "Winner");
        String str = result.toString();
        assertNotNull(str);
        assertTrue(str.contains("P1") || str.contains("Winner"));
    }
}
