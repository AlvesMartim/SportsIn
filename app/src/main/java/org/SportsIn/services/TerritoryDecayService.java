package org.SportsIn.services;

import org.SportsIn.model.Arene;
import org.SportsIn.repository.AreneRepository;
import org.SportsIn.weather.WeatherClassifier;
import org.SportsIn.weather.WeatherClient;
import org.SportsIn.weather.WeatherConditionTag;
import org.SportsIn.weather.WeatherForecastEntry;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

@Service
public class TerritoryDecayService {

    private static final double NATURAL_DECAY_PER_DAY = 3.0;
    private static final double EXTREME_ACCELERATION_PER_DAY = 8.0;

    private final AreneRepository areneRepository;
    private final TerritoryInfluenceStateService influenceStateService;
    private final WeatherClient weatherClient;

    public TerritoryDecayService(AreneRepository areneRepository,
                                 TerritoryInfluenceStateService influenceStateService,
                                 WeatherClient weatherClient) {
        this.areneRepository = areneRepository;
        this.influenceStateService = influenceStateService;
        this.weatherClient = weatherClient;
    }

    public void applyDailyDecay() {
        List<Arene> arenas = areneRepository.findAll();

        for (Arene arena : arenas) {
            if (arena.getControllingTeamId() == null) {
                continue;
            }

            double totalDecay = NATURAL_DECAY_PER_DAY;
            if (isProlongedExtremeWeather(arena)) {
                totalDecay += EXTREME_ACCELERATION_PER_DAY;
            }

            double level = influenceStateService.decay(arena.getId(), totalDecay);
            if (level <= 0.0) {
                arena.setControllingTeam(null);
                areneRepository.save(arena);
            }
        }
    }

    private boolean isProlongedExtremeWeather(Arene arena) {
        List<WeatherForecastEntry> forecast = weatherClient.getForecast(arena.getLatitude(), arena.getLongitude(), 48);
        if (forecast.isEmpty()) {
            return false;
        }

        int sampleSize = Math.min(16, forecast.size());
        int extremeSlots = 0;

        for (int i = 0; i < sampleSize; i++) {
            EnumSet<WeatherConditionTag> tags = WeatherClassifier.classify(forecast.get(i));
            if (tags.contains(WeatherConditionTag.EXTREME)) {
                extremeSlots++;
            }
        }

        // At least 12 out of 16 forecast slots (~36h over the next 48h).
        return sampleSize >= 12 && extremeSlots >= 12;
    }
}
