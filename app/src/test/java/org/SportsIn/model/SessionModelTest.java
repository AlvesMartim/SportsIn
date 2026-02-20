package org.SportsIn.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionModelTest {

    @Test
    void defaultConstructor() {
        Session session = new Session();
        assertNull(session.getId());
        assertNull(session.getSport());
    }

    @Test
    void fullConstructor() {
        Sport sport = new Sport();
        sport.setCode("BASKET");
        LocalDateTime now = LocalDateTime.now();

        Session session = new Session("S1", sport, "arena1", SessionState.ACTIVE, now,
                List.of(new Participant("1", "Rouge", ParticipantType.TEAM)));

        assertEquals("S1", session.getId());
        assertEquals("BASKET", session.getSport().getCode());
        assertEquals("arena1", session.getPointId());
        assertEquals(SessionState.ACTIVE, session.getState());
        assertEquals(now, session.getCreatedAt());
        assertEquals(1, session.getParticipants().size());
    }

    @Test
    void gettersAndSetters() {
        Session session = new Session();
        session.setId("S2");
        session.setState(SessionState.TERMINATED);
        session.setWinnerParticipantId("P1");
        session.setEndedAt(LocalDateTime.now());

        assertEquals("S2", session.getId());
        assertEquals(SessionState.TERMINATED, session.getState());
        assertEquals("P1", session.getWinnerParticipantId());
        assertNotNull(session.getEndedAt());
    }

    @Test
    void result_getterSetter() {
        Session session = new Session();
        SessionResult result = new SessionResult();
        result.setMetrics(List.of(new MetricValue("1", MetricType.GOALS, 3, "match")));
        session.setResult(result);

        assertNotNull(session.getResult());
        assertEquals(1, session.getResult().getMetrics().size());
    }

    @Test
    void toString_containsId() {
        Session session = new Session();
        session.setId("S1");
        assertTrue(session.toString().contains("S1"));
    }
}
