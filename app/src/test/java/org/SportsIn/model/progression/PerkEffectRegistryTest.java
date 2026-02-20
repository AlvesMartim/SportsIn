package org.SportsIn.model.progression;

import org.SportsIn.model.progression.effects.BoostEffect;
import org.SportsIn.model.progression.effects.PerkEffectRegistry;
import org.SportsIn.model.progression.effects.PerkEffectStrategy;
import org.SportsIn.model.progression.effects.ShieldEffect;
import org.SportsIn.model.progression.effects.XpMultiplierEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PerkEffectRegistryTest {

    private PerkEffectRegistry registry;

    @BeforeEach
    void setUp() {
        List<PerkEffectStrategy> strategies = List.of(
                new BoostEffect(),
                new ShieldEffect(),
                new XpMultiplierEffect()
        );
        registry = new PerkEffectRegistry(strategies);
    }

    @Test
    void resolve_knownType_returnsStrategy() {
        PerkEffectStrategy strategy = registry.resolve("INFLUENCE_BOOST");
        assertNotNull(strategy);
        assertEquals("INFLUENCE_BOOST", strategy.getEffectType());
    }

    @Test
    void resolve_shield_returnsStrategy() {
        PerkEffectStrategy strategy = registry.resolve("INFLUENCE_REDUCTION");
        assertNotNull(strategy);
        assertEquals("INFLUENCE_REDUCTION", strategy.getEffectType());
    }

    @Test
    void resolve_xpMultiplier_returnsStrategy() {
        PerkEffectStrategy strategy = registry.resolve("XP_MULTIPLIER");
        assertNotNull(strategy);
        assertEquals("XP_MULTIPLIER", strategy.getEffectType());
    }

    @Test
    void resolve_unknownType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> registry.resolve("UNKNOWN_TYPE"));
    }

    @Test
    void hasEffect_knownType_returnsTrue() {
        assertTrue(registry.hasEffect("INFLUENCE_BOOST"));
        assertTrue(registry.hasEffect("INFLUENCE_REDUCTION"));
        assertTrue(registry.hasEffect("XP_MULTIPLIER"));
    }

    @Test
    void hasEffect_unknownType_returnsFalse() {
        assertFalse(registry.hasEffect("DOES_NOT_EXIST"));
    }
}
