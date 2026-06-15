package org.SportsIn.strava;

import org.SportsIn.services.StravaRateLimiter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StravaRateLimiterTest {

    @Test
    void tryAcquire_underLimit_returnsTrue() {
        StravaRateLimiter limiter = new StravaRateLimiter(10, 100);
        assertTrue(limiter.tryAcquire());
    }

    @Test
    void tryAcquire_atLimit15Min_returnsFalse() {
        StravaRateLimiter limiter = new StravaRateLimiter(3, 1000);
        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire());
        // 4ème dépasse la limite de 15 min
        assertFalse(limiter.tryAcquire());
    }

    @Test
    void tryAcquire_atDailyLimit_returnsFalse() {
        StravaRateLimiter limiter = new StravaRateLimiter(1000, 3);
        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire());
        // 4ème dépasse la limite journalière
        assertFalse(limiter.tryAcquire());
    }

    @Test
    void getCount15Min_incrementsCorrectly() {
        StravaRateLimiter limiter = new StravaRateLimiter(100, 1000);
        assertEquals(0, limiter.getCount15Min());
        limiter.tryAcquire();
        limiter.tryAcquire();
        assertEquals(2, limiter.getCount15Min());
    }

    @Test
    void getCountDay_incrementsCorrectly() {
        StravaRateLimiter limiter = new StravaRateLimiter(100, 1000);
        assertEquals(0, limiter.getCountDay());
        limiter.tryAcquire();
        assertEquals(1, limiter.getCountDay());
    }

    @Test
    void getLimits_returnConfiguredValues() {
        StravaRateLimiter limiter = new StravaRateLimiter(42, 999);
        assertEquals(42, limiter.getLimitPer15Min());
        assertEquals(999, limiter.getLimitPerDay());
    }

    @Test
    void defaultConstructor_usesStravaDefaults() {
        StravaRateLimiter limiter = new StravaRateLimiter();
        assertEquals(100, limiter.getLimitPer15Min());
        assertEquals(1000, limiter.getLimitPerDay());
    }
}
