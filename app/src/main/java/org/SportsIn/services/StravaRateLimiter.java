package org.SportsIn.services;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate limiter respectant les limites Strava API :
 * - 100 requêtes par fenêtre de 15 minutes
 * - 1000 requêtes par jour
 *
 * Implémentation in-memory (fenêtres glissantes).
 * Suffisant pour une instance unique ; pour le multi-instance utiliser Redis.
 */
@Component
public class StravaRateLimiter {

    private static final long WINDOW_15MIN_MS = 15 * 60 * 1000L;

    private final int limitPer15Min;
    private final int limitPerDay;

    private final AtomicInteger count15Min = new AtomicInteger(0);
    private final AtomicLong window15MinStart = new AtomicLong(Instant.now().toEpochMilli());

    private final AtomicInteger countDay = new AtomicInteger(0);
    private volatile LocalDate currentDay = LocalDate.now(ZoneOffset.UTC);

    public StravaRateLimiter() {
        this.limitPer15Min = 100;
        this.limitPerDay = 1000;
    }

    public StravaRateLimiter(int limitPer15Min, int limitPerDay) {
        this.limitPer15Min = limitPer15Min;
        this.limitPerDay = limitPerDay;
    }

    /**
     * Tente de consommer un slot de requête.
     * @return true si la requête peut être effectuée, false si le rate limit est atteint
     */
    public synchronized boolean tryAcquire() {
        long now = Instant.now().toEpochMilli();

        // Réinitialiser la fenêtre de 15 min si nécessaire
        if (now - window15MinStart.get() >= WINDOW_15MIN_MS) {
            count15Min.set(0);
            window15MinStart.set(now);
        }

        // Réinitialiser le compteur journalier si on a changé de jour UTC
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        if (!today.equals(currentDay)) {
            countDay.set(0);
            currentDay = today;
        }

        if (count15Min.get() >= limitPer15Min) return false;
        if (countDay.get() >= limitPerDay) return false;

        count15Min.incrementAndGet();
        countDay.incrementAndGet();
        return true;
    }

    public int getCount15Min() { return count15Min.get(); }
    public int getCountDay() { return countDay.get(); }
    public int getLimitPer15Min() { return limitPer15Min; }
    public int getLimitPerDay() { return limitPerDay; }
}
