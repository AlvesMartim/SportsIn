package org.SportsIn.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricValueTest {

    @Test
    void defaultConstructor() {
        MetricValue mv = new MetricValue();
        assertNull(mv.getParticipantId());
        assertNull(mv.getMetricType());
        assertEquals(0.0, mv.getValue());
        assertNull(mv.getContext());
    }

    @Test
    void parameterizedConstructor() {
        MetricValue mv = new MetricValue("P1", MetricType.GOALS, 5.0, "match1");
        assertEquals("P1", mv.getParticipantId());
        assertEquals(MetricType.GOALS, mv.getMetricType());
        assertEquals(5.0, mv.getValue());
        assertEquals("match1", mv.getContext());
    }

    @Test
    void settersAndGetters() {
        MetricValue mv = new MetricValue();
        mv.setParticipantId("P2");
        mv.setMetricType(MetricType.TIME_SECONDS);
        mv.setValue(120.5);
        mv.setContext("race1");

        assertEquals("P2", mv.getParticipantId());
        assertEquals(MetricType.TIME_SECONDS, mv.getMetricType());
        assertEquals(120.5, mv.getValue());
        assertEquals("race1", mv.getContext());
    }

    @Test
    void toString_containsInfo() {
        MetricValue mv = new MetricValue("P1", MetricType.REPS, 10.0, "squat");
        String str = mv.toString();
        assertNotNull(str);
    }

    @Test
    void allMetricTypes() {
        for (MetricType type : MetricType.values()) {
            MetricValue mv = new MetricValue("P1", type, 1.0, "ctx");
            assertEquals(type, mv.getMetricType());
        }
    }
}
