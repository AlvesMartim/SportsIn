package org.SportsIn.scheduler;

import org.SportsIn.services.TerritoryDecayService;
import org.SportsIn.services.WeatherFlashMissionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "weather.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class WeatherScheduler {

    private final WeatherFlashMissionService weatherFlashMissionService;
    private final TerritoryDecayService territoryDecayService;

    public WeatherScheduler(WeatherFlashMissionService weatherFlashMissionService,
                            TerritoryDecayService territoryDecayService) {
        this.weatherFlashMissionService = weatherFlashMissionService;
        this.territoryDecayService = territoryDecayService;
    }

    /**
     * Every 6 hours, inspect forecasts and generate flash weather missions.
     */
    @Scheduled(cron = "0 0 */6 * * *", zone = "Europe/Paris")
    public void generateWeatherFlashMissions() {
        weatherFlashMissionService.generateFlashMissionsForAllTeams();
    }

    /**
     * Every day at 03:15, apply natural and weather-accelerated territory decay.
     */
    @Scheduled(cron = "0 15 3 * * *", zone = "Europe/Paris")
    public void applyTerritoryDecay() {
        territoryDecayService.applyDailyDecay();
    }
}
