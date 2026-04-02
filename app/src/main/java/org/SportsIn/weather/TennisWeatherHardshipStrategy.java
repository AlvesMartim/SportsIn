package org.SportsIn.weather;

import org.springframework.stereotype.Component;

@Component
public class TennisWeatherHardshipStrategy implements WeatherHardshipStrategy {

    @Override
    public String sportCode() {
        return "TENNIS";
    }

    @Override
    public double hardshipIndex(WeatherSnapshot snapshot) {
        if (snapshot == null || snapshot.isUnknown()) {
            return 1.0;
        }

        double tempPenalty = Math.max(heatSeverity(snapshot.temperatureC()), coldSeverity(snapshot.temperatureC()));
        double rainPenalty = rainSeverity(snapshot.precipitationMm());
        double windPenalty = windSeverity(snapshot.windSpeedMps());
        double stormPenalty = stormSeverity(snapshot.weatherCode());

        double index = 1.0
                + (tempPenalty * 0.15)
                + (rainPenalty * 0.25)
                + (windPenalty * 0.40)
                + (stormPenalty * 0.20);

        return clampIndex(index);
    }
}
