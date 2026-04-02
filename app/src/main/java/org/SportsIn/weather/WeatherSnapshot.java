package org.SportsIn.weather;

import java.time.Instant;

public record WeatherSnapshot(
        double temperatureC,
        double windSpeedMps,
        double precipitationMm,
        int weatherCode,
        String weatherMain,
        String description,
        Instant observedAt
) {
    public static WeatherSnapshot unknown() {
        return new WeatherSnapshot(0.0, 0.0, 0.0, 800, "UNKNOWN", "unknown", Instant.now());
    }

    public boolean isUnknown() {
        return "UNKNOWN".equalsIgnoreCase(weatherMain);
    }
}
