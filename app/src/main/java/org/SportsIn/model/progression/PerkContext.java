package org.SportsIn.model.progression;

import java.util.Map;

public record PerkContext(
    Long teamId,
    Long opponentTeamId,
    String targetPointId,
    double baseInfluence,
    Map<String, Object> parameters
) {}
