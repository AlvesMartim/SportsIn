package org.SportsIn.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest {

    @Test
    void defaultConstructor() {
        Participant p = new Participant();
        assertNull(p.getId());
        assertNull(p.getName());
        assertNull(p.getType());
    }

    @Test
    void parameterizedConstructor() {
        Participant p = new Participant("1", "Rouge", ParticipantType.TEAM);
        assertEquals("1", p.getId());
        assertEquals("Rouge", p.getName());
        assertEquals(ParticipantType.TEAM, p.getType());
    }

    @Test
    void settersAndGetters() {
        Participant p = new Participant();
        p.setId("2");
        p.setName("Joueur A");
        p.setType(ParticipantType.PLAYER);

        assertEquals("2", p.getId());
        assertEquals("Joueur A", p.getName());
        assertEquals(ParticipantType.PLAYER, p.getType());
    }

    @Test
    void toString_containsInfo() {
        Participant p = new Participant("1", "Test", ParticipantType.TEAM);
        String str = p.toString();
        assertNotNull(str);
    }

    @Test
    void participantTypes() {
        assertEquals(2, ParticipantType.values().length);
        assertNotNull(ParticipantType.valueOf("PLAYER"));
        assertNotNull(ParticipantType.valueOf("TEAM"));
    }
}
