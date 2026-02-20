package org.SportsIn.model.progression;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ActivePerkTest {

    @Test
    void isActive_whenWithinRange() {
        ActivePerk perk = new ActivePerk();
        perk.setActivatedAt(Instant.now().minusSeconds(60).toString());
        perk.setExpiresAt(Instant.now().plusSeconds(3600).toString());
        assertTrue(perk.isActive());
    }

    @Test
    void isActive_whenExpired() {
        ActivePerk perk = new ActivePerk();
        perk.setActivatedAt(Instant.now().minusSeconds(7200).toString());
        perk.setExpiresAt(Instant.now().minusSeconds(3600).toString());
        assertFalse(perk.isActive());
    }

    @Test
    void isExpired_whenPast() {
        ActivePerk perk = new ActivePerk();
        perk.setActivatedAt(Instant.now().minusSeconds(7200).toString());
        perk.setExpiresAt(Instant.now().minusSeconds(10).toString());
        assertTrue(perk.isExpired());
    }

    @Test
    void isExpired_whenNotYet() {
        ActivePerk perk = new ActivePerk();
        perk.setActivatedAt(Instant.now().minusSeconds(60).toString());
        perk.setExpiresAt(Instant.now().plusSeconds(3600).toString());
        assertFalse(perk.isExpired());
    }

    @Test
    void getRemainingDuration_whenActive() {
        ActivePerk perk = new ActivePerk();
        perk.setActivatedAt(Instant.now().minusSeconds(60).toString());
        perk.setExpiresAt(Instant.now().plusSeconds(3600).toString());
        Duration remaining = perk.getRemainingDuration();
        assertTrue(remaining.getSeconds() > 3500);
    }

    @Test
    void getRemainingDuration_whenExpired_isZero() {
        ActivePerk perk = new ActivePerk();
        perk.setActivatedAt(Instant.now().minusSeconds(7200).toString());
        perk.setExpiresAt(Instant.now().minusSeconds(10).toString());
        assertEquals(Duration.ZERO, perk.getRemainingDuration());
    }

    @Test
    void getExpiresAtInstant() {
        Instant future = Instant.now().plusSeconds(3600);
        ActivePerk perk = new ActivePerk();
        perk.setExpiresAt(future.toString());
        assertNotNull(perk.getExpiresAtInstant());
        assertEquals(future, perk.getExpiresAtInstant());
    }

    @Test
    void getExpiresAtInstant_nullReturnsNull() {
        ActivePerk perk = new ActivePerk();
        assertNull(perk.getExpiresAtInstant());
    }

    @Test
    void getActivatedAtInstant_nullReturnsNull() {
        ActivePerk perk = new ActivePerk();
        assertNull(perk.getActivatedAtInstant());
    }

    @Test
    void gettersAndSetters() {
        ActivePerk perk = new ActivePerk();
        perk.setId(1L);
        perk.setTeamId(10L);
        perk.setPerkDefinitionId(20L);
        perk.setTargetId("arena1");
        perk.setUsageCount(3);

        assertEquals(1L, perk.getId());
        assertEquals(10L, perk.getTeamId());
        assertEquals(20L, perk.getPerkDefinitionId());
        assertEquals("arena1", perk.getTargetId());
        assertEquals(3, perk.getUsageCount());
    }
}
