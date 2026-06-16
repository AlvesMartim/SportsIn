package org.SportsIn.weather;

import java.time.Instant;

public record WeatherForecastEntry(
        Instant at,
        double temperatureC,
        double windSpeedMps,
        double precipitationMm,
        int weatherCode,
        String weatherMain,
        String description
) {
}
