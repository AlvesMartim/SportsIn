package org.SportsIn.weather;

import org.springframework.stereotype.Component;

@Component
public class BasketWeatherHardshipStrategy implements WeatherHardshipStrategy {

    @Override
    public String sportCode() {
        return "BASKET";
    }

    @Override
    public double hardshipIndex(WeatherSnapshot snapshot) {
        if (snapshot == null || snapshot.isUnknown()) {
            return 1.0;
        }

        double tempPenalty = Math.max(heatSeverity(snapshot.temperatureC()), coldSeverity(snapshot.temperatureC()));
        double rainPenalty = rainSeverity(snapshot.precipitationMm());
        double windPenalty = windSeverity(snapshot.windSpeedMps());

        double index = 1.0
                + (tempPenalty * 0.12)
                + (rainPenalty * 0.05)
                + (windPenalty * 0.08);

        return clampIndex(index);
    }
}
