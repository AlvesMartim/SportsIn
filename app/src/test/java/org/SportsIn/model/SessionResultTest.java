package org.SportsIn.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionResultTest {

    @Test
    void defaultConstructor() {
        SessionResult result = new SessionResult();
        assertNull(result.getSession());
        assertNull(result.getMetrics());
    }

    @Test
    void parameterizedConstructor() {
        Session session = new Session();
        session.setId("S1");
        List<MetricValue> metrics = List.of(
                new MetricValue("P1", MetricType.GOALS, 3, "match"));

        SessionResult result = new SessionResult(session, metrics);
        assertEquals(session, result.getSession());
        assertEquals(1, result.getMetrics().size());
        assertEquals(3.0, result.getMetrics().get(0).getValue());
    }

    @Test
    void settersAndGetters() {
        SessionResult result = new SessionResult();
        Session session = new Session();
        session.setId("S2");
        result.setSession(session);
        result.setMetrics(List.of());

        assertEquals("S2", result.getSession().getId());
        assertTrue(result.getMetrics().isEmpty());
    }
}
