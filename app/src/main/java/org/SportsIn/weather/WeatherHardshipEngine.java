package org.SportsIn.weather;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WeatherHardshipEngine {

    private final Map<String, WeatherHardshipStrategy> strategyBySport;
    private final WeatherHardshipStrategy defaultStrategy;

    public WeatherHardshipEngine(List<WeatherHardshipStrategy> strategies) {
        this.strategyBySport = strategies.stream()
                .filter(strategy -> !"*".equals(strategy.sportCode()))
                .collect(Collectors.toMap(
                        strategy -> strategy.sportCode().toUpperCase(Locale.ROOT),
                        Function.identity()
                ));

        this.defaultStrategy = strategies.stream()
                .filter(strategy -> "*".equals(strategy.sportCode()))
                .findFirst()
                .orElseGet(DefaultWeatherHardshipStrategy::new);
    }

    public double computeHardshipIndex(String sportCode, WeatherSnapshot snapshot) {
        if (snapshot == null || snapshot.isUnknown()) {
            return 1.0;
        }

        String key = sportCode != null ? sportCode.toUpperCase(Locale.ROOT) : "";
        WeatherHardshipStrategy strategy = strategyBySport.getOrDefault(key, defaultStrategy);
        return strategy.hardshipIndex(snapshot);
    }
}
