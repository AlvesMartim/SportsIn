package org.SportsIn.weather;

import org.SportsIn.model.Arene;
import org.SportsIn.model.Session;
import org.SportsIn.repository.AreneRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SessionWeatherService {

    private final AreneRepository areneRepository;
    private final WeatherClient weatherClient;
    private final WeatherHardshipEngine hardshipEngine;

    public SessionWeatherService(AreneRepository areneRepository,
                                 WeatherClient weatherClient,
                                 WeatherHardshipEngine hardshipEngine) {
        this.areneRepository = areneRepository;
        this.weatherClient = weatherClient;
        this.hardshipEngine = hardshipEngine;
    }

    public SessionWeatherImpact analyze(Session session) {
        if (session == null || session.getPointId() == null || session.getSport() == null) {
            return SessionWeatherImpact.neutral("NO_SESSION_CONTEXT");
        }

        Optional<Arene> areneOpt = areneRepository.findById(session.getPointId());
        if (areneOpt.isEmpty()) {
            return SessionWeatherImpact.neutral("UNKNOWN_ARENA");
        }

        Arene arene = areneOpt.get();
        Optional<WeatherSnapshot> snapshotOpt = weatherClient.getCurrentWeather(arene.getLatitude(), arene.getLongitude());
        if (snapshotOpt.isEmpty()) {
            return SessionWeatherImpact.neutral("WEATHER_UNAVAILABLE");
        }

        WeatherSnapshot snapshot = snapshotOpt.get();
        double hardshipIndex = hardshipEngine.computeHardshipIndex(session.getSport().getCode(), snapshot);
        double weatherBonus = Math.max(0.0, hardshipIndex - 1.0);

        return new SessionWeatherImpact(
                snapshot,
                hardshipIndex,
                weatherBonus,
                WeatherClassifier.classify(snapshot),
                "OPENWEATHER_CURRENT"
        );
    }
}
