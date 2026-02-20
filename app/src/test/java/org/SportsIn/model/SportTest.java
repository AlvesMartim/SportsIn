package org.SportsIn.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SportTest {

    @Test
    void defaultConstructor() {
        Sport sport = new Sport();
        assertNull(sport.getId());
        assertNull(sport.getCode());
        assertNull(sport.getName());
    }

    @Test
    void parameterizedConstructor() {
        Sport sport = new Sport(1L, "FOOTBALL", "Football", 101L, null);
        assertEquals(1L, sport.getId());
        assertEquals("FOOTBALL", sport.getCode());
        assertEquals("Football", sport.getName());
        assertEquals(101L, sport.getVictoryRuleId());
        assertNull(sport.getScoringRuleId());
    }

    @Test
    void settersAndGetters() {
        Sport sport = new Sport();
        sport.setId(2L);
        sport.setCode("BASKET");
        sport.setName("Basketball");
        sport.setVictoryRuleId(102L);
        sport.setScoringRuleId(201L);

        assertEquals(2L, sport.getId());
        assertEquals("BASKET", sport.getCode());
        assertEquals("Basketball", sport.getName());
        assertEquals(102L, sport.getVictoryRuleId());
        assertEquals(201L, sport.getScoringRuleId());
    }

    @Test
    void toString_containsCode() {
        Sport sport = new Sport();
        sport.setCode("RUNNING");
        String str = sport.toString();
        assertNotNull(str);
        assertTrue(str.contains("RUNNING"));
    }
}
