package org.SportsIn.services;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TerritoryInfluenceStateService {

    private static final double DEFAULT_INFLUENCE_LEVEL = 60.0;
    private static final double MAX_INFLUENCE_LEVEL = 100.0;

    private final Map<String, Double> influenceByArenaId = new ConcurrentHashMap<>();

    public double getInfluenceLevel(String arenaId) {
        return influenceByArenaId.computeIfAbsent(arenaId, ignored -> DEFAULT_INFLUENCE_LEVEL);
    }

    public double reinforce(String arenaId, double amount) {
        return adjust(arenaId, Math.abs(amount));
    }

    public double decay(String arenaId, double amount) {
        return adjust(arenaId, -Math.abs(amount));
    }

    public double setLevel(String arenaId, double value) {
        double clamped = clamp(value);
        influenceByArenaId.put(arenaId, clamped);
        return clamped;
    }

    public void reset(String arenaId) {
        influenceByArenaId.remove(arenaId);
    }

    private double adjust(String arenaId, double delta) {
        double current = getInfluenceLevel(arenaId);
        double next = clamp(current + delta);
        influenceByArenaId.put(arenaId, next);
        return next;
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(MAX_INFLUENCE_LEVEL, value));
    }
}
