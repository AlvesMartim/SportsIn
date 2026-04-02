package org.SportsIn.weather;

import org.springframework.stereotype.Component;

@Component
public class MusculationWeatherHardshipStrategy implements WeatherHardshipStrategy {

    @Override
    public String sportCode() {
        return "MUSCULATION";
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
                + (tempPenalty * 0.10)
                + (rainPenalty * 0.03)
                + (windPenalty * 0.03);

        return clampIndex(index);
    }
}
