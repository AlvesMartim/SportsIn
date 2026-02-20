package org.SportsIn.model.progression;

import org.SportsIn.model.progression.effects.XpMultiplierEffect;
import org.SportsIn.model.user.Equipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class XpMultiplierEffectTest {

    private XpMultiplierEffect effect;

    @BeforeEach
    void setUp() {
        effect = new XpMultiplierEffect();
    }

    @Test
    void getEffectType_returnsXpMultiplier() {
        assertEquals("XP_MULTIPLIER", effect.getEffectType());
    }

    @Test
    void computeInfluenceModifier_alwaysZero() {
        // XP multiplier doesn't affect influence
        PerkContext ctx = new PerkContext(1L, 2L, "point1", 100.0,
                Map.of("multiplier", 2.0));
        assertEquals(0.0, effect.computeInfluenceModifier(ctx), 0.001);
    }

    @Test
    void computeInfluenceModifier_zeroBase_alwaysZero() {
        PerkContext ctx = new PerkContext(1L, 2L, "point1", 0.0, Map.of());
        assertEquals(0.0, effect.computeInfluenceModifier(ctx), 0.001);
    }

    @Test
    void canActivate_levelSufficient_noExisting() {
        Equipe team = new Equipe("test");
        team.setXp(5000);

        PerkDefinition def = makeDef(1, 2, 3600, 0);
        assertTrue(effect.canActivate(team, def, Collections.emptyList()));
    }

    @Test
    void canActivate_levelTooLow() {
        Equipe team = new Equipe("test");
        team.setXp(0);

        PerkDefinition def = makeDef(10, 2, 3600, 0);
        assertFalse(effect.canActivate(team, def, Collections.emptyList()));
    }

    @Test
    void canActivate_maxInstancesReached() {
        Equipe team = new Equipe("test");
        team.setXp(5000);

        PerkDefinition def = makeDef(1, 1, 0, 0);
        def.setId(20L);

        ActivePerk ap = new ActivePerk();
        ap.setPerkDefinitionId(20L);
        ap.setActivatedAt(Instant.now().minusSeconds(60).toString());
        ap.setExpiresAt(Instant.now().plusSeconds(3600).toString());

        assertFalse(effect.canActivate(team, def, List.of(ap)));
    }

    private PerkDefinition makeDef(int requiredLevel, int maxInstances, long duration, long cooldown) {
        PerkDefinition def = new PerkDefinition();
        def.setId(20L);
        def.setCode("XP_MULT");
        def.setName("XP Multiplier");
        def.setEffectType("XP_MULTIPLIER");
        def.setRequiredLevel(requiredLevel);
        def.setMaxActiveInstances(maxInstances);
        def.setDurationSeconds(duration);
        def.setCooldownSeconds(cooldown);
        def.setStackable(false);
        return def;
    }
}
