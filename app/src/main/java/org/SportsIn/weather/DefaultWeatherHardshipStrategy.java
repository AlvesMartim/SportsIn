package org.SportsIn.weather;

import org.springframework.stereotype.Component;

@Component
public class DefaultWeatherHardshipStrategy implements WeatherHardshipStrategy {

    @Override
    public String sportCode() {
        return "*";
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
                + (tempPenalty * 0.20)
                + (rainPenalty * 0.20)
                + (windPenalty * 0.20)
                + (stormPenalty * 0.15);

        return clampIndex(index);
    }
}
