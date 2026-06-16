package org.SportsIn.weather;

import java.util.EnumSet;
import java.util.Set;

public final class WeatherClassifier {

    private WeatherClassifier() {
    }

    public static EnumSet<WeatherConditionTag> classify(WeatherSnapshot snapshot) {
        return classify(snapshot.temperatureC(), snapshot.windSpeedMps(), snapshot.precipitationMm(), snapshot.weatherCode());
    }

    public static EnumSet<WeatherConditionTag> classify(WeatherForecastEntry forecastEntry) {
        return classify(
                forecastEntry.temperatureC(),
                forecastEntry.windSpeedMps(),
                forecastEntry.precipitationMm(),
                forecastEntry.weatherCode()
        );
    }

    private static EnumSet<WeatherConditionTag> classify(double tempC,
                                                         double windSpeedMps,
                                                         double precipitationMm,
                                                         int weatherCode) {
        EnumSet<WeatherConditionTag> tags = EnumSet.noneOf(WeatherConditionTag.class);

        if (precipitationMm >= 1.5) {
            tags.add(WeatherConditionTag.RAIN);
        }
        if (tempC >= 30.0) {
            tags.add(WeatherConditionTag.HEAT);
        }
        if (tempC <= 5.0) {
            tags.add(WeatherConditionTag.COLD);
        }
        if (windSpeedMps >= 10.0) {
            tags.add(WeatherConditionTag.WIND);
        }
        if (weatherCode >= 200 && weatherCode <= 299) {
            tags.add(WeatherConditionTag.STORM);
        }

        if (isExtreme(tempC, windSpeedMps, precipitationMm, weatherCode)) {
            tags.add(WeatherConditionTag.EXTREME);
        }

        return tags;
    }

    public static boolean isExtreme(Set<WeatherConditionTag> tags) {
        return tags != null && tags.contains(WeatherConditionTag.EXTREME);
    }

    public static String dominantLabel(Set<WeatherConditionTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return "CHANGE_BRUTAL";
        }
        if (tags.contains(WeatherConditionTag.STORM)) return "ORAGE";
        if (tags.contains(WeatherConditionTag.HEAT)) return "CANICULE";
        if (tags.contains(WeatherConditionTag.COLD)) return "FROID";
        if (tags.contains(WeatherConditionTag.WIND)) return "VENT";
        if (tags.contains(WeatherConditionTag.RAIN)) return "PLUIE";
        return "CHANGE_BRUTAL";
    }

    private static boolean isExtreme(double tempC, double windSpeedMps, double precipitationMm, int weatherCode) {
        return (tempC >= 35.0)
                || (tempC <= -3.0)
                || (windSpeedMps >= 17.0)
                || (precipitationMm >= 10.0)
                || (weatherCode >= 200 && weatherCode <= 299);
    }
}
