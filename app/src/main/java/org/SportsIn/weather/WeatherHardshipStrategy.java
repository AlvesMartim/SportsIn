package org.SportsIn.weather;

public interface WeatherHardshipStrategy {

    String sportCode();

    double hardshipIndex(WeatherSnapshot snapshot);

    default double normalize(double value, double min, double max) {
        if (max <= min) {
            return 0.0;
        }
        double normalized = (value - min) / (max - min);
        return Math.max(0.0, Math.min(1.0, normalized));
    }

    default double heatSeverity(double tempC) {
        return normalize(tempC, 26.0, 40.0);
    }

    default double coldSeverity(double tempC) {
        return normalize(8.0 - tempC, 0.0, 13.0);
    }

    default double rainSeverity(double precipitationMm) {
        return normalize(precipitationMm, 0.5, 15.0);
    }

    default double windSeverity(double windSpeedMps) {
        return normalize(windSpeedMps, 5.0, 20.0);
    }

    default double stormSeverity(int weatherCode) {
        return (weatherCode >= 200 && weatherCode <= 299) ? 1.0 : 0.0;
    }

    default double clampIndex(double index) {
        return Math.max(1.0, Math.min(2.2, index));
    }
}
