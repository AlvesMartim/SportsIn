package org.SportsIn.weather;

import java.util.List;
import java.util.Optional;

public interface WeatherClient {

    Optional<WeatherSnapshot> getCurrentWeather(double latitude, double longitude);

    List<WeatherForecastEntry> getForecast(double latitude, double longitude, int nextHours);
}
