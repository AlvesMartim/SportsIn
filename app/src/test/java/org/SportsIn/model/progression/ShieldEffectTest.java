package org.SportsIn.model.progression;

import org.SportsIn.model.progression.effects.ShieldEffect;
import org.SportsIn.model.user.Equipe;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ShieldEffectTest {

    private final ShieldEffect shield = new ShieldEffect();

    @Test
    void effectType_is_INFLUENCE_REDUCTION() {
        assertEquals("INFLUENCE_REDUCTION", shield.getEffectType());
    }

    @Test
    void reduces_influence_by_configured_percent() {
        PerkContext ctx = new PerkContext(1L, 2L, 10L, 0.20,
                Map.of("reductionPercent", 50));

        double modifier = shield.computeInfluenceModifier(ctx);
        assertEquals(-0.10, modifier, 0.001);
    }

    @Test
    void defaults_to_50_percent_if_no_parameter() {
        PerkContext ctx = new PerkContext(1L, 2L, 10L, 0.20, Map.of());

        double modifier = shield.computeInfluenceModifier(ctx);
        assertEquals(-0.10, modifier, 0.001);
    }

    @Test
    void zero_base_influence_yields_zero_reduction() {
        PerkContext ctx = new PerkContext(1L, 2L, 10L, 0.0,
                Map.of("reductionPercent", 50));

        assertEquals(0.0, shield.computeInfluenceModifier(ctx), 0.001);
    }

    @Test
    void full_reduction_with_100_percent() {
        PerkContext ctx = new PerkContext(1L, 2L, 10L, 0.20,
                Map.of("reductionPercent", 100));

        assertEquals(-0.20, shield.computeInfluenceModifier(ctx), 0.001);
    }

    @Test
    void canActivate_rejects_below_required_level() {
        Equipe team = new Equipe("TestTeam");
        team.setXp(50); // Level 1

        PerkDefinition def = new PerkDefinition();
        def.setId(1L);
        def.setRequiredLevel(3); // Needs 300 XP
        def.setCooldownSeconds(0);
        def.setMaxActiveInstances(1);

        assertFalse(shield.canActivate(team, def, List.of()));
    }

    @Test
    void canActivate_allows_at_required_level() {
        Equipe team = new Equipe("TestTeam");
        team.setXp(300); // Level 3

        PerkDefinition def = new PerkDefinition();
        def.setId(1L);
        def.setRequiredLevel(3);
        def.setCooldownSeconds(0);
        def.setMaxActiveInstances(1);

        assertTrue(shield.canActivate(team, def, List.of()));
    }

    @Test
    void canActivate_rejects_when_max_instances_reached() {
        Equipe team = new Equipe("TestTeam");
        team.setXp(1000); // Level 5

        PerkDefinition def = new PerkDefinition();
        def.setId(1L);
        def.setRequiredLevel(3);
        def.setCooldownSeconds(0);
        def.setMaxActiveInstances(1);

        // One active perk already
        ActivePerk existing = new ActivePerk();
        existing.setPerkDefinitionId(1L);
        existing.setActivatedAt(Instant.now().minusSeconds(100).toString());
        existing.setExpiresAt(Instant.now().plusSeconds(10000).toString());

        assertFalse(shield.canActivate(team, def, List.of(existing)));
    }

    @Test
    void canActivate_rejects_during_cooldown() {
        Equipe team = new Equipe("TestTeam");
        team.setXp(1000); // Level 5

        PerkDefinition def = new PerkDefinition();
        def.setId(1L);
        def.setRequiredLevel(3);
        def.setCooldownSeconds(604800); // 7 days
        def.setMaxActiveInstances(1);

        // Expired perk (recently)
        ActivePerk expired = new ActivePerk();
        expired.setPerkDefinitionId(1L);
        expired.setActivatedAt(Instant.now().minusSeconds(300000).toString());
        expired.setExpiresAt(Instant.now().minusSeconds(100).toString()); // Expired 100s ago

        // Cooldown is 7 days, expired 100s ago -> still in cooldown
        assertFalse(shield.canActivate(team, def, List.of(expired)));
    }
}
