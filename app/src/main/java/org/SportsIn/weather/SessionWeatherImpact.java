package org.SportsIn.weather;

import java.util.Set;
import java.util.stream.Collectors;

public record SessionWeatherImpact(
        WeatherSnapshot snapshot,
        double hardshipIndex,
        double weatherInfluenceBonus,
        Set<WeatherConditionTag> tags,
        String source
) {
    public static SessionWeatherImpact neutral(String source) {
        return new SessionWeatherImpact(
                WeatherSnapshot.unknown(),
                1.0,
                0.0,
                Set.of(),
                source
        );
    }

    public String tagsAsCsv() {
        return tags.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }
}
