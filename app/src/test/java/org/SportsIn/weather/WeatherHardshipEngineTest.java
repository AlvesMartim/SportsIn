package org.SportsIn.weather;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherHardshipEngineTest {

    @Test
    void tennis_is_more_sensitive_to_wind_than_football() {
        WeatherHardshipEngine engine = new WeatherHardshipEngine(List.of(
                new DefaultWeatherHardshipStrategy(),
                new FootballWeatherHardshipStrategy(),
                new TennisWeatherHardshipStrategy(),
                new BasketWeatherHardshipStrategy(),
                new MusculationWeatherHardshipStrategy()
        ));

        WeatherSnapshot windy = new WeatherSnapshot(
                18.0,
                18.0,
                0.0,
                800,
                "Clear",
                "clear sky",
                Instant.now()
        );

        double tennis = engine.computeHardshipIndex("TENNIS", windy);
        double football = engine.computeHardshipIndex("FOOTBALL", windy);

        assertTrue(tennis > football);
    }

    @Test
    void musculation_is_less_sensitive_than_football_in_rain() {
        WeatherHardshipEngine engine = new WeatherHardshipEngine(List.of(
                new DefaultWeatherHardshipStrategy(),
                new FootballWeatherHardshipStrategy(),
                new TennisWeatherHardshipStrategy(),
                new BasketWeatherHardshipStrategy(),
                new MusculationWeatherHardshipStrategy()
        ));

        WeatherSnapshot rainy = new WeatherSnapshot(
                11.0,
                9.0,
                8.0,
                500,
                "Rain",
                "moderate rain",
                Instant.now()
        );

        double football = engine.computeHardshipIndex("FOOTBALL", rainy);
        double musculation = engine.computeHardshipIndex("MUSCULATION", rainy);

        assertTrue(football > musculation);
    }

    @Test
    void unknown_sport_uses_default_strategy() {
        WeatherHardshipEngine engine = new WeatherHardshipEngine(List.of(
                new DefaultWeatherHardshipStrategy(),
                new FootballWeatherHardshipStrategy()
        ));

        WeatherSnapshot heat = new WeatherSnapshot(
                36.0,
                4.0,
                0.0,
                800,
                "Clear",
                "hot",
                Instant.now()
        );

        double index = engine.computeHardshipIndex("UNKNOWN_SPORT", heat);
        assertTrue(index > 1.0);
    }
}
