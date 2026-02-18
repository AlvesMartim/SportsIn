package org.SportsIn.services;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InfluenceCalculatorTest {

    @Test
    void chains_modifiers_in_order() {
        InfluenceModifier route = new InfluenceModifier() {
            @Override
            public double apply(Long teamId, Long pointId, double current) {
                return current + 0.10;
            }
            @Override
            public int getOrder() { return 10; }
        };

        InfluenceModifier perk = new InfluenceModifier() {
            @Override
            public double apply(Long teamId, Long pointId, double current) {
                return current - 0.05;
            }
            @Override
            public int getOrder() { return 20; }
        };

        InfluenceCalculator calc = new InfluenceCalculator(List.of(perk, route)); // Unordered
        double result = calc.computeTotalModifier(1L, 10L);

        // Should be ordered: route first (+0.10), then perk (-0.05)
        assertEquals(0.05, result, 0.001);
    }

    @Test
    void no_modifiers_returns_zero() {
        InfluenceCalculator calc = new InfluenceCalculator(List.of());
        assertEquals(0.0, calc.computeTotalModifier(1L, 10L), 0.001);
    }

    @Test
    void single_modifier() {
        InfluenceModifier route = new InfluenceModifier() {
            @Override
            public double apply(Long teamId, Long pointId, double current) {
                return current + 0.15;
            }
            @Override
            public int getOrder() { return 10; }
        };

        InfluenceCalculator calc = new InfluenceCalculator(List.of(route));
        assertEquals(0.15, calc.computeTotalModifier(1L, 10L), 0.001);
    }

    @Test
    void modifier_receives_accumulated_value() {
        InfluenceModifier first = new InfluenceModifier() {
            @Override
            public double apply(Long teamId, Long pointId, double current) {
                return current + 0.20;
            }
            @Override
            public int getOrder() { return 10; }
        };

        InfluenceModifier second = new InfluenceModifier() {
            @Override
            public double apply(Long teamId, Long pointId, double current) {
                // Halve the accumulated value
                return current * 0.5;
            }
            @Override
            public int getOrder() { return 20; }
        };

        InfluenceCalculator calc = new InfluenceCalculator(List.of(first, second));
        assertEquals(0.10, calc.computeTotalModifier(1L, 10L), 0.001);
    }
}
