package org.SportsIn.model.progression;

import org.SportsIn.model.progression.effects.BoostEffect;
import org.SportsIn.model.user.Equipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BoostEffectTest {

    private BoostEffect boostEffect;

    @BeforeEach
    void setUp() {
        boostEffect = new BoostEffect();
    }

    @Test
    void getEffectType_returnsInfluenceBoost() {
        assertEquals("INFLUENCE_BOOST", boostEffect.getEffectType());
    }

    // ---- computeInfluenceModifier ----

    @Test
    void computeInfluenceModifier_withConfiguredPercent() {
        PerkContext ctx = new PerkContext(1L, 2L, "point1", 100.0,
                Map.of("boostPercent", 30.0));
        double modifier = boostEffect.computeInfluenceModifier(ctx);
        assertEquals(30.0, modifier, 0.001); // 100 * 30 / 100
    }

    @Test
    void computeInfluenceModifier_defaultPercent() {
        PerkContext ctx = new PerkContext(1L, 2L, "point1", 200.0,
                Map.of()); // no boostPercent → default 25%
        double modifier = boostEffect.computeInfluenceModifier(ctx);
        assertEquals(50.0, modifier, 0.001); // 200 * 25 / 100
    }

    @Test
    void computeInfluenceModifier_zeroBase() {
        PerkContext ctx = new PerkContext(1L, 2L, "point1", 0.0,
                Map.of("boostPercent", 50.0));
        assertEquals(0.0, boostEffect.computeInfluenceModifier(ctx), 0.001);
    }

    @Test
    void computeInfluenceModifier_integerParam() {
        PerkContext ctx = new PerkContext(1L, 2L, "point1", 100.0,
                Map.of("boostPercent", 10)); // Integer, not Double
        assertEquals(10.0, boostEffect.computeInfluenceModifier(ctx), 0.001);
    }

    // ---- canActivate ----

    @Test
    void canActivate_levelTooLow() {
        Equipe team = new Equipe("test");
        team.setXp(0); // level 1

        PerkDefinition def = makeDef(5, 1, 3600, 0);
        assertFalse(boostEffect.canActivate(team, def, Collections.emptyList()));
    }

    @Test
    void canActivate_levelSufficient() {
        Equipe team = new Equipe("test");
        team.setXp(5000); // should be high enough level

        PerkDefinition def = makeDef(1, 2, 3600, 0);
        assertTrue(boostEffect.canActivate(team, def, Collections.emptyList()));
    }

    @Test
    void canActivate_maxInstancesReached() {
        Equipe team = new Equipe("test");
        team.setXp(5000);

        PerkDefinition def = makeDef(1, 1, 0, 0);
        def.setId(10L);

        // Create an active perk
        ActivePerk ap = new ActivePerk();
        ap.setPerkDefinitionId(10L);
        ap.setActivatedAt(Instant.now().minusSeconds(60).toString());
        ap.setExpiresAt(Instant.now().plusSeconds(3600).toString());

        assertFalse(boostEffect.canActivate(team, def, List.of(ap)));
    }

    @Test
    void canActivate_duringCooldown() {
        Equipe team = new Equipe("test");
        team.setXp(5000);

        PerkDefinition def = makeDef(1, 2, 7200, 3600); // cooldown 1 hour
        def.setId(10L);

        // Recently expired perk
        ActivePerk ap = new ActivePerk();
        ap.setPerkDefinitionId(10L);
        ap.setActivatedAt(Instant.now().minusSeconds(7200).toString());
        ap.setExpiresAt(Instant.now().minusSeconds(10).toString()); // expired 10s ago, cooldown 3600s

        assertFalse(boostEffect.canActivate(team, def, List.of(ap)));
    }

    private PerkDefinition makeDef(int requiredLevel, int maxInstances, long duration, long cooldown) {
        PerkDefinition def = new PerkDefinition();
        def.setId(10L);
        def.setCode("BOOST");
        def.setName("Boost");
        def.setEffectType("INFLUENCE_BOOST");
        def.setRequiredLevel(requiredLevel);
        def.setMaxActiveInstances(maxInstances);
        def.setDurationSeconds(duration);
        def.setCooldownSeconds(cooldown);
        def.setStackable(false);
        return def;
    }
}
